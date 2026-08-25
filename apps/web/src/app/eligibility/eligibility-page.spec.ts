import { TestBed } from '@angular/core/testing';
import { EligibilityService } from '../core/services/eligibility.service';
import { MockEligibilityService } from '../core/services/mock-eligibility.service';
import { EligibilityPageComponent } from './eligibility-page';

function flush(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

function todayIso(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

describe('EligibilityPageComponent', () => {
  let mock: MockEligibilityService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EligibilityPageComponent],
      providers: [MockEligibilityService, { provide: EligibilityService, useExisting: MockEligibilityService }],
    }).compileComponents();
    mock = TestBed.inject(MockEligibilityService);
    mock.latencyMs = 0;
  });

  function createComponent() {
    const fixture = TestBed.createComponent(EligibilityPageComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('defaults Check Coverage On to the current date', () => {
    const { componentInstance: component } = createComponent();
    expect(component.form.controls.checkDate.value).toBe(todayIso());
  });

  it('rejects a blank Member ID without displaying a business result', () => {
    const { componentInstance: component } = createComponent();
    component.onSubmit();
    expect(component.idError()).toBe('Enter a member ID.');
    expect(component.hasFieldErrors()).toBe(true);
    expect(component.result()).toBeNull();
  });

  it('rejects a missing or invalid Check Coverage On date', () => {
    const { componentInstance: component } = createComponent();
    component.form.setValue({ memberId: 'M-1001', checkDate: '' });
    component.onSubmit();
    expect(component.dateError()).toBe('Enter a valid coverage date.');
  });

  it('shows a loading state and suppresses a duplicate submission while pending', async () => {
    const { componentInstance: component } = createComponent();
    component.form.setValue({ memberId: 'M-1001', checkDate: '2026-06-01' });
    component.onSubmit();
    expect(component.loading()).toBe(true);

    const spy = vi.spyOn(mock, 'checkEligibility');
    component.onSubmit();
    expect(spy).not.toHaveBeenCalled();

    await flush();
    expect(component.loading()).toBe(false);
  });

  it('resolves ELIGIBLE for M-1001 on its coverage effective date', async () => {
    const { componentInstance: component } = createComponent();
    component.form.setValue({ memberId: 'M-1001', checkDate: '2025-01-01' });
    component.onSubmit();
    await flush();
    expect(component.result()?.eligibilityStatus).toBe('ELIGIBLE');
    expect(component.showResult()).toBe(true);
  });

  it('resolves NOT_YET_ELIGIBLE for M-1002 before its coverage effective date', async () => {
    const { componentInstance: component } = createComponent();
    component.form.setValue({ memberId: 'M-1002', checkDate: '2026-01-01' });
    component.onSubmit();
    await flush();
    expect(component.result()?.eligibilityStatus).toBe('NOT_YET_ELIGIBLE');
  });

  it('resolves INELIGIBLE for M-1003 after its coverage termination date', async () => {
    const { componentInstance: component } = createComponent();
    component.form.setValue({ memberId: 'M-1003', checkDate: '2026-01-01' });
    component.onSubmit();
    await flush();
    expect(component.result()?.eligibilityStatus).toBe('INELIGIBLE');
  });

  it('resolves MEMBER_NOT_FOUND for the intentionally absent M-9999 and omits empty detail rows', async () => {
    const { componentInstance: component } = createComponent();
    component.form.setValue({ memberId: 'M-9999', checkDate: '2026-01-01' });
    component.onSubmit();
    await flush();
    expect(component.result()?.eligibilityStatus).toBe('MEMBER_NOT_FOUND');
    expect(component.resultRows().some((row) => row.label === 'Member name')).toBe(false);
    expect(component.resultRows().some((row) => row.label === 'Member ID')).toBe(true);
  });

  it('shows a generic unavailable-service state on failure and recovers via Try Again', async () => {
    const { componentInstance: component } = createComponent();
    mock.forceError = true;
    component.form.setValue({ memberId: 'M-1001', checkDate: '2025-01-01' });
    component.onSubmit();
    await flush();
    expect(component.showError()).toBe(true);
    expect(component.result()).toBeNull();

    mock.forceError = false;
    component.onRetry();
    await flush();
    expect(component.showError()).toBe(false);
    expect(component.result()?.eligibilityStatus).toBe('ELIGIBLE');
  });

  it('clears the result and restores the current-date default on Start Another Inquiry', async () => {
    const { componentInstance: component } = createComponent();
    component.form.setValue({ memberId: 'M-1001', checkDate: '2025-01-01' });
    component.onSubmit();
    await flush();
    expect(component.result()).not.toBeNull();

    component.onReset();
    expect(component.result()).toBeNull();
    expect(component.form.controls.memberId.value).toBe('');
    expect(component.form.controls.checkDate.value).toBe(todayIso());
  });

  it('marks the invalid field with aria-invalid so the error is announced', () => {
    const fixture = createComponent();
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();
    const input: HTMLInputElement = fixture.nativeElement.querySelector('#memberId');
    expect(input.getAttribute('aria-invalid')).toBe('true');
  });
});
