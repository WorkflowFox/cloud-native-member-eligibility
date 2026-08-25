import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { formatReadableDate, isValidIsoDate, todayIsoDate } from '../core/date-utils';
import { EligibilityResult } from '../core/models/eligibility.model';
import { EligibilityService } from '../core/services/eligibility.service';
import { MockEligibilityService } from '../core/services/mock-eligibility.service';

interface ResultRow {
  label: string;
  value: string;
}

interface DemoState {
  label: string;
  go: () => void;
}

@Component({
  selector: 'app-eligibility-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './eligibility-page.html',
  styleUrl: './eligibility-page.css',
})
export class EligibilityPageComponent implements OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly eligibilityService = inject(EligibilityService);
  /** Present only while the mock service is provided; drives the prototype controls bar. */
  protected readonly mockService = inject(MockEligibilityService, { optional: true });

  private readonly today = todayIsoDate();
  private requestSubscription?: Subscription;

  readonly form = this.fb.nonNullable.group({
    memberId: [''],
    checkDate: [this.today],
  });

  readonly loading = signal(false);
  readonly result = signal<EligibilityResult | null>(null);
  readonly serviceError = signal(false);
  readonly idError = signal('');
  readonly dateError = signal('');
  readonly pendingId = signal('');
  readonly pendingDate = signal('');
  readonly forceError = signal(false);

  readonly hasFieldErrors = computed(() => !!(this.idError() || this.dateError()));
  readonly summaryText = computed(() => [this.idError(), this.dateError()].filter(Boolean).join(' '));
  readonly idInvalid = computed(() => !!this.idError());
  readonly dateInvalid = computed(() => !!this.dateError());
  readonly idHelp = computed(() => this.idError() || 'Exact match only — no name or partial search.');
  readonly dateHelp = computed(() => this.dateError() || 'Defaults to today. Format YYYY-MM-DD.');
  readonly submitLabel = computed(() => (this.loading() ? 'Checking eligibility…' : 'Check Eligibility'));

  readonly showResult = computed(() => !!this.result() && !this.loading());
  readonly showError = computed(() => this.serviceError() && !this.loading());
  readonly showEmpty = computed(() => !this.result() && !this.serviceError() && !this.loading());

  readonly resultRows = computed<ResultRow[]>(() => {
    const r = this.result();
    if (!r) return [];
    const rows: ResultRow[] = [{ label: 'Member ID', value: r.memberId }];
    if (r.eligibilityStatus !== 'MEMBER_NOT_FOUND') {
      rows.push({ label: 'Member name', value: r.memberName ?? '' });
      rows.push({ label: 'Plan name', value: r.planName ?? '' });
      rows.push({
        label: 'Coverage effective',
        value: r.coverageEffectiveDate ? formatReadableDate(r.coverageEffectiveDate) : '',
      });
      rows.push({
        label: 'Coverage termination',
        value: r.coverageTerminationDate ? formatReadableDate(r.coverageTerminationDate) : 'No termination date',
      });
    }
    rows.push({ label: 'Checked coverage on', value: formatReadableDate(r.checkCoverageOnDate) });
    return rows;
  });

  /** Only populated when running against the mock service; hidden entirely once HTTP integration lands. */
  readonly demoStates: DemoState[] = this.mockService
    ? [
        { label: 'Eligible', go: () => this.runDemo('M-1001') },
        { label: 'Not yet eligible', go: () => this.runDemo('M-1002') },
        { label: 'Ineligible', go: () => this.runDemo('M-1003') },
        { label: 'Not found', go: () => this.runDemo('M-9999') },
        { label: 'Validation', go: () => this.runValidationDemo() },
        { label: 'Loading', go: () => this.runLoadingDemo() },
        { label: 'Service error', go: () => this.runServiceErrorDemo() },
        { label: 'Reset', go: () => this.onReset() },
      ]
    : [];

  onToggleForceError(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.forceError.set(checked);
    if (this.mockService) this.mockService.forceError = checked;
  }

  onSubmit(): void {
    if (this.loading()) return;
    const memberId = this.form.controls.memberId.value.trim();
    const checkDate = this.form.controls.checkDate.value;
    const idError = memberId ? '' : 'Enter a member ID.';
    const dateError = isValidIsoDate(checkDate) ? '' : 'Enter a valid coverage date.';
    this.idError.set(idError);
    this.dateError.set(dateError);
    if (idError || dateError) {
      this.result.set(null);
      this.serviceError.set(false);
      return;
    }
    this.run(memberId, checkDate);
  }

  onRetry(): void {
    const memberId = this.form.controls.memberId.value.trim();
    const checkDate = this.form.controls.checkDate.value;
    this.run(memberId, checkDate);
  }

  onReset(): void {
    this.requestSubscription?.unsubscribe();
    this.form.reset({ memberId: '', checkDate: this.today });
    this.loading.set(false);
    this.result.set(null);
    this.serviceError.set(false);
    this.idError.set('');
    this.dateError.set('');
    queueMicrotask(() => document.getElementById('memberId')?.focus());
  }

  ngOnDestroy(): void {
    this.requestSubscription?.unsubscribe();
  }

  private run(memberId: string, checkDate: string): void {
    this.requestSubscription?.unsubscribe();
    this.loading.set(true);
    this.result.set(null);
    this.serviceError.set(false);
    this.pendingId.set(memberId);
    this.pendingDate.set(isValidIsoDate(checkDate) ? formatReadableDate(checkDate) : checkDate);
    this.requestSubscription = this.eligibilityService.checkEligibility({ memberId, checkDate }).subscribe({
      next: (result) => {
        this.loading.set(false);
        this.result.set(result);
      },
      error: () => {
        this.loading.set(false);
        this.serviceError.set(true);
      },
    });
  }

  private runDemo(memberId: string): void {
    this.requestSubscription?.unsubscribe();
    if (this.mockService) this.mockService.forceError = false;
    this.forceError.set(false);
    this.idError.set('');
    this.dateError.set('');
    this.form.patchValue({ memberId, checkDate: this.today });
    this.run(memberId, this.today);
  }

  private runValidationDemo(): void {
    this.requestSubscription?.unsubscribe();
    this.loading.set(false);
    this.result.set(null);
    this.serviceError.set(false);
    this.form.patchValue({ memberId: '', checkDate: '' });
    this.onSubmit();
  }

  private runLoadingDemo(): void {
    this.requestSubscription?.unsubscribe();
    this.form.patchValue({ memberId: 'M-1001', checkDate: this.today });
    this.idError.set('');
    this.dateError.set('');
    this.result.set(null);
    this.serviceError.set(false);
    this.pendingId.set('M-1001');
    this.pendingDate.set(formatReadableDate(this.today));
    this.loading.set(true);
  }

  private runServiceErrorDemo(): void {
    if (this.mockService) this.mockService.forceError = true;
    this.forceError.set(true);
    this.form.patchValue({ memberId: 'M-1001', checkDate: this.today });
    this.idError.set('');
    this.dateError.set('');
    this.run('M-1001', this.today);
  }
}
