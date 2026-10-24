import { Component, inject, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.html'
})
export class Login implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly checking = signal(true);
  readonly busy = signal(false);
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    email: ['', [
      Validators.required,
      Validators.email,
      Validators.maxLength(254)
    ]],
    password: ['', Validators.required]
  });

  async ngOnInit(): Promise<void> {
    try {
      const user = await this.auth.restoreSession();

      await this.router.navigateByUrl(
        user.mustChangePassword ? '/change-password' : '/dashboard'
      );
    } catch (error) {
      if (!(error instanceof HttpErrorResponse && error.status === 401)) {
        this.error.set(apiErrorMessage(error));
      }
    } finally {
      this.checking.set(false);
    }
  }

  async submit(): Promise<void> {
    if (this.busy() || this.checking()) {
      return;
    }

    this.form.controls.email.setValue(
      this.form.controls.email.value.trim()
    );

    this.form.markAllAsTouched();

    if (this.form.invalid) {
      return;
    }

    this.busy.set(true);
    this.error.set('');

    try {
      const user = await this.auth.login(this.form.getRawValue());

      await this.router.navigateByUrl(
        user.mustChangePassword ? '/change-password' : '/dashboard'
      );
    } catch (error) {
      this.error.set(apiErrorMessage(error));
    } finally {
      this.form.controls.password.reset('');
      this.busy.set(false);
    }
  }
}