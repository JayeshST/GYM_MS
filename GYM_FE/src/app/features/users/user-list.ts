import {
  Component,
  DestroyRef,
  inject,
  signal
} from '@angular/core';

import { DatePipe } from '@angular/common';

import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { AccessService } from '../../core/services/access.service';
import {
  catchError,
  EMPTY,
  startWith,
  Subject,
  switchMap,
  tap
} from 'rxjs';

import {
  FlagFilter,
  UserDetails,
  UserListItem,
  UserPage,
  UserRoleSummary,
  UserSearch
} from '../../core/models/user.models';

import {
  UserService,
  userErrorMessage
} from '../../core/services/user.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DatePipe
  ],
  templateUrl: './user-list.html',
  styleUrl: './user-list.scss'
})
export class UserList {
  readonly access = inject(AccessService);
  private readonly service = inject(UserService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  private readonly requests = new Subject<UserSearch>();
  private readonly detailRequests = new Subject<number | null>();

  readonly loading = signal(false);
  readonly error = signal('');
  readonly pageData = signal<UserPage | null>(null);

  readonly detailOpen = signal(false);
  readonly detailLoading = signal(false);
  readonly detailError = signal('');
  readonly selectedUser = signal<UserDetails | null>(null);

  readonly form = new FormGroup({
    search: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.maxLength(254)
        
      ]
    }),

    isInactive: new FormControl<FlagFilter>('', {
      nonNullable: true,
      validators: [
        Validators.pattern(/^(0|1)?$/)
      ]
    }),

    isDeleted: new FormControl<FlagFilter>('0', {
      nonNullable: true,
      validators: [
        Validators.pattern(/^(0|1)?$/)
      ]
    }),

    size: new FormControl(20, {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.min(1),
        Validators.max(100)
      ]
    })
  });

  private appliedQuery: UserSearch = {
    search: '',
    isInactive: '',
    isDeleted: '0',
    page: 0,
    size: 20
  };
  constructor() {
    this.requests.pipe(
      startWith(this.appliedQuery),

      tap(() => {
        this.loading.set(true);
        this.error.set('');
        this.pageData.set(null);
      }),

      switchMap(query =>
        this.service.getUsers(query).pipe(
          tap(page => {
            this.pageData.set(page);
            this.loading.set(false);
          }),

          catchError((error: unknown) => {
            this.error.set(this.handleError(error));
            this.loading.set(false);

            return EMPTY;
          })
        )
      ),

      takeUntilDestroyed(this.destroyRef)
    ).subscribe();

    this.detailRequests.pipe(
      switchMap(userId => {
        if (userId === null) {
          return EMPTY;
        }

        return this.service.getUser(userId).pipe(
          tap(user => {
            this.selectedUser.set(user);
            this.detailLoading.set(false);
          }),

          catchError((error: unknown) => {
            this.detailError.set(this.handleError(error));
            this.detailLoading.set(false);

            return EMPTY;
          })
        );
      }),

      takeUntilDestroyed(this.destroyRef)
    ).subscribe();
  }

  search(): void {
    this.form.markAllAsTouched();

    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    const size = Number(value.size);

    if (!Number.isInteger(size) || size < 1 || size > 100) {
      this.form.controls.size.setErrors({
        invalidPageSize: true
      });
      return;
    }

    this.appliedQuery = {
      search: value.search.trim(),
      isInactive: value.isInactive,
      isDeleted: value.isDeleted,
      size,
      page: 0
    };

    this.closeDetails();
    this.requests.next({ ...this.appliedQuery });
  }

  reset(): void {
    this.form.reset({
      search: '',
      isInactive: '',
      isDeleted: '0',
      size: 20
    });

    this.search();
  }

  reload(): void {
    if (this.loading()) {
      return;
    }

    this.closeDetails();
    this.requests.next({ ...this.appliedQuery });
  }

  changePage(page: number): void {
    const current = this.pageData();

    if (
      this.loading() ||
      !current ||
      !Number.isInteger(page) ||
      page < 0 ||
      page >= current.totalPages
    ) {
      return;
    }

    this.appliedQuery = {
      ...this.appliedQuery,
      page
    };

    this.closeDetails();
    this.requests.next({ ...this.appliedQuery });
  }

  viewDetails(userId: number): void {
    this.selectedUser.set(null);
    this.detailError.set('');
    this.detailOpen.set(true);

    if (!Number.isSafeInteger(userId) || userId <= 0) {
      this.detailRequests.next(null);
      this.detailLoading.set(false);
      this.detailError.set(
        'Unable to open this user. Please refresh the list and try again.'
      );
      return;
    }

    this.detailLoading.set(true);
    this.detailRequests.next(userId);
  }

  closeDetails(): void {
    // Cancels any pending details request.
    this.detailRequests.next(null);

    this.detailOpen.set(false);
    this.detailLoading.set(false);
    this.selectedUser.set(null);
    this.detailError.set('');
  }

  statusLabel(
    user: Pick<UserListItem, 'isInactive' | 'isDeleted'>
  ): string {
    if (user.isDeleted === 1) {
      return 'Deleted';
    }

    if (user.isInactive === 1) {
      return 'Inactive';
    }

    return 'Active';
  }

  roleLabel(role: UserRoleSummary): string {
    const name = role.roleName || role.roleCode;

    if (
      role.isInactive === 1 &&
      role.mappingIsInactive === 1
    ) {
      return `${name} — role and assignment inactive`;
    }

    if (role.isInactive === 1) {
      return `${name} — role inactive`;
    }

    if (role.mappingIsInactive === 1) {
      return `${name} — assignment inactive`;
    }

    return name;
  }
private handleError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 401) {
      this.closeDetails();
      this.access.clear();

      void this.router.navigateByUrl('/login');
    } else if (error.status === 403) {
      this.closeDetails();
      this.access.clear();

      void this.router.navigateByUrl('/access-denied');
    }
  }

  return userErrorMessage(error);

  }

}