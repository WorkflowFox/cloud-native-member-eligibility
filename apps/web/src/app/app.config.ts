import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { EligibilityService } from './core/services/eligibility.service';
import { MockEligibilityService } from './core/services/mock-eligibility.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // Stage 2: Angular frontend with a mock eligibility service (spec §18, Stage 2).
    // Both tokens resolve to the same singleton so demo controls can reach the
    // mock directly. Stage 5 swaps this pair for an HttpEligibilityService
    // provider without touching any component.
    MockEligibilityService,
    { provide: EligibilityService, useExisting: MockEligibilityService },
  ],
};
