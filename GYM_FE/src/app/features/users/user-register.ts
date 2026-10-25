import {
  Component,
  DestroyRef,
  inject,
  signal
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import {
  RegisterUserResponse,
  StaffRole
} from '../../core/models/user.models';

import {
  UserRegistrationService
} from '../../core/services/user-registration.service';

const nonBlank: ValidatorFn = control => {
  const value = String(control.value ?? '');

  return value.trim().length > 0
    ? null
    : { blank: true };
};

const passwordBytes: ValidatorFn = control => {
  const value = String(control.value ?? '');

  return new TextEncoder().encode(value).length <= 72
    ? null
    : { passwordBytes: true };
};

const passwordsMatch: ValidatorFn = group => {
  return group.get('password')?.value ===
    group.get('confirmPassword')?.value
    ? null
    : { passwordMismatch: true };
};

@Component({
  selector: 'app-user-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './user-register.html',
  styleUrl: './user-register.scss'
})
export class UserRegister {
  private readonly service = inject(UserRegistrationService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly busy = signal(false);
  readonly error = signal('');
  readonly createdUser = signal<RegisterUserResponse | null>(null);

  readonly form = new FormGroup(
    {
      fullName: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          nonBlank,
          Validators.maxLength(150)
        ]
      }),

      email: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.email,
          Validators.maxLength(254)
        ]
      }),

      mobileNumber: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.pattern(/^\+?[0-9]{10,15}$/)
        ]
      }),

      address: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          nonBlank,
          Validators.maxLength(500)
        ]
      }),

      roleCode: new FormControl<StaffRole | ''>('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.pattern(/^(TRAINER|RECEPTIONIST)$/)
        ]
      }),

      password: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          nonBlank,
          Validators.minLength(12),
          Validators.maxLength(72),
          passwordBytes
        ]
      }),

      confirmPassword: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.maxLength(72),
          passwordBytes
        ]
      })
    },
    {
      validators: passwordsMatch
    }
  );

  invalid(control: AbstractControl): boolean {
    return control.touched && control.invalid;
  }

  submit(): void {
    if (this.busy() || this.createdUser()) {
      return;
    }

    const value = this.form.getRawValue();

    // Passwords are intentionally not trimmed.
    this.form.patchValue({
      fullName: value.fullName.trim(),
      email: value.email.trim().toLowerCase(),
      mobileNumber: value.mobileNumber.trim(),
      address: value.address.trim()
    });

    this.form.markAllAsTouched();
    this.error.set('');

    if (this.form.invalid) {
      return;
    }

    const request = this.form.getRawValue();

    if (
      request.roleCode !== 'TRAINER' &&
      request.roleCode !== 'RECEPTIONIST'
    ) {
      return;
    }

    this.busy.set(true);

    this.service.register({
      ...request,
      roleCode: request.roleCode
    }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => {
        this.busy.set(false);
        this.form.controls.password.reset('');
        this.form.controls.confirmPassword.reset('');
      })
    ).subscribe({
      next: user => {
        this.createdUser.set(user);
        this.form.reset();
      },

      error: (error: unknown) => {
        this.error.set(this.getErrorMessage(error));

        if (
          error instanceof HttpErrorResponse &&
          error.status === 401
        ) {
          void this.router.navigateByUrl('/login');
        }
      }
    });
  }

  addAnother(): void {
    this.form.reset();
    this.error.set('');
    this.createdUser.set(null);
  }

  private getErrorMessage(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
  return 'We could not confirm whether the account was created. Please check the user list before trying again.';
}

    switch (error.status) {
  case 0:
    return 'We could not confirm whether the account was created. Please check the user list before trying again.';

  case 200:
  case 201:
    return 'We could not read the confirmation. Please check the user list to see if the account was created.';

  case 400:
    return 'Please check the details you entered and make sure both passwords match and meet the password requirements.';

  case 401:
    return 'Your session has expired. Please sign in again.';

  case 403:
    return 'You are not allowed to create this account right now. Please sign in again. If the problem continues, contact your gym administrator.';

  case 409:
    return 'An account with these details may already exist. Please check the user list or contact your gym administrator.';

  case 429:
    return 'Too many attempts. Please wait a moment before trying again.';

  default:
    return 'We could not confirm whether the account was created. Please check the user list before trying again.';
}
  }
}