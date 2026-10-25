import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-access-denied',
  standalone: true,
  imports: [RouterLink],
  template: `
    <main class="access-page">
      <section aria-labelledby="access-title">
        <p class="label">GYM MS</p>

        <h1 id="access-title">
          {{ verificationFailed
            ? 'Access could not be verified'
            : 'Access unavailable' }}
        </h1>

        @if (verificationFailed) {
          <p>
            We could not check your access right now.
            Return to your dashboard and try again.
          </p>
        } @else {
          <p>
            You do not have access to this page.
            Contact your gym administrator if you need access.
          </p>
        }

        <a routerLink="/dashboard">Back to dashboard</a>
      </section>
    </main>
  `,
  styles: `
    :host {
      display: block;
      color: #eef4ef;
      background: #0c1410;
    }

    .access-page {
      display: grid;
      min-height: 100dvh;
      place-items: center;
      padding: 24px;
      box-sizing: border-box;
    }

    section {
      width: 100%;
      max-width: 480px;
      padding: 32px;
      box-sizing: border-box;
      border: 1px solid #304438;
      border-radius: 20px;
      background: #132019;
    }

    .label {
      color: #d4f58a;
      font-size: 12px;
      letter-spacing: 2px;
    }

    h1 {
      margin: 16px 0;
      font-size: 30px;
    }

    p {
      color: #adbbb1;
      line-height: 1.7;
    }

    a {
      display: inline-flex;
      margin-top: 16px;
      padding: 12px 20px;
      border-radius: 10px;
      color: #17200f;
      background: #d4f58a;
      font-weight: 700;
      text-decoration: none;
    }

    a:focus-visible {
      outline: 3px solid #eef4ef;
      outline-offset: 4px;
    }
  `
})
export class AccessDenied {
  private readonly route = inject(ActivatedRoute);

  readonly verificationFailed =
    this.route.snapshot.queryParamMap.get('reason') === 'verification';
}