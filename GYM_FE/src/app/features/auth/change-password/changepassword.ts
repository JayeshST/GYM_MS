import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

const passwordBytesValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const value = control.value;

  if (typeof value !== 'string' || value.length === 0) {
    return null;
  }

  return value.trim().length === 0 ||
    new TextEncoder().encode(value).length > 72
      ? { passwordBytes: true }
      : null;
};

const matchingPasswords: ValidatorFn = (
  group: AbstractControl
): ValidationErrors | null => {
  return group.get('newPassword')?.value ===
    group.get('confirmPassword')?.value
      ? null
      : { passwordMismatch: true };
};

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './change-password.html'
})
export class ChangePassword {
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly busy = signal(false);
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group(
    {
      currentPassword: ['', Validators.required],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(12),
        Validators.maxLength(72),
        passwordBytesValidator
      ]],
      confirmPassword: ['', Validators.required]
    },
    { validators: matchingPasswords }
  );

  async submit(): Promise<void> {
    if (this.busy()) {
      return;
    }

    this.form.markAllAsTouched();

    if (this.form.invalid) {
      return;
    }

    this.busy.set(true);
    this.error.set('');

    try {
      await this.auth.changePassword(this.form.getRawValue());
      this.form.reset();

      // Backend requires a new login after password change.
      await this.router.navigateByUrl('/login');
    } catch (error) {
      this.error.set(apiErrorMessage(error));
    } finally {
      this.busy.set(false);
    }
  }

  async logout(): Promise<void> {
    if (this.busy()) {
      return;
    }

    this.busy.set(true);
    this.error.set('');

    try {
      await this.auth.logout();
      await this.router.navigateByUrl('/login');
    } catch (error) {
      this.error.set(apiErrorMessage(error));
    } finally {
      this.busy.set(false);
    }
  }
}