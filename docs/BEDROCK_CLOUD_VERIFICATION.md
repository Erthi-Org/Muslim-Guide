# Bedrock cloud verification — 2026-11-19
Branch integration/claude-bedrock-cloud-20261119, based on main commit 7e833f3235e876df3508df08e3801bbe3a8b4880.
No application or default-branch changes.

## Verified
Repository variable NEXVARY_BEDROCK_ROLE_ARN = arn:aws:iam::716997672148:role/erthi-github-bedrock-development.
GitHub OIDC issuer token.actions.githubusercontent.com; audience sts.amazonaws.com.
Trust corrected to support immutable Erthi-Org owner ID 354032667 while retaining the integration/claude-bedrock-cloud-* branch restriction.
Actual subject: repo:Erthi-Org@354032667/Muslim-Guide@1450238018:ref:refs/heads/integration/claude-bedrock-cloud-20261119.
STS passed: https://github.com/Erthi-Org/Muslim-Guide/actions/runs/44478063596 (attempt 2).
Claude Code CLI 2.1.328; Bedrock eu-central-1; eu.anthropic.claude-sonnet-4-6.
Verified exact response ERTHI_BEDROCK_OK:
https://github.com/Erthi-Org/Muslim-Guide/actions/runs/44478324756
362 input and 13 output tokens; one turn; estimated list-price cost USD 0.001281, not reconciled AWS billing.
Minimal model call ran only after successful STS, in an empty temporary workspace, no tools/repository modifications.
15-minute timeout, 900-second temporary credentials, concurrency 1, no automatic paid retry.
Owner removed monetary ceiling; this does not authorize uncontrolled automatic task chains.

## Not verified / limitations
Official Claude GitHub App not installed on this repository; no App permissions changed.
This test verifies the official CLI in Actions, not official App issue/PR automation.
Normal Run workflow UI requires workflow on default branch. No default-branch modification/merge authorized.
Branch-only owner push bootstraps auth; explicit [bedrock-smoke] commit changing the workflow triggers one minimal smoke on attempt 1.
No GitHub actions:write permission granted, no permanent AWS keys.
No application development, build, APK, CI repair, deployment or release tested here.

## Rollback
Revert the isolated integration commits or close the draft PR. Remove only the repository variable if abandoning this pilot.
Shared AWS role/provider removal needs impact checking first.
Original trust matched repo:Erthi-Org/*:ref:refs/heads/integration/claude-bedrock-cloud-*; corrected policy also matches repo:Erthi-Org@354032667/* with the same branch restriction.
