# WKS Platform v2 — Architecture

> **Status: in progress.** This document will contain the full decision log for
> the v2 architecture. It will be completed as part of the Phase-0 epic documentation
> pass (after Stories 1.x stabilise the skeleton).

## Overview

WKS Platform v2 is a single-container monolith: Spring Boot backend with an embedded
CIB seven BPMN engine behind a `WorkflowEngine` port, a React SPA frontend bundled into
the same JAR, H2 in development / PostgreSQL in production via JPA + Flyway.

See `CLAUDE.md` for the full package structure, critical architectural rules, and tech stack.
