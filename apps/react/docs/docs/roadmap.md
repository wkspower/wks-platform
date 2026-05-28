---
sidebar_position: 2
---

# Roadmap

The WKS Platform roadmap outlines our vision for providing a robust, modern, and secure Business Process Management (BPM) and Case Management solution. This roadmap is a statement of intent and priorities. It is not a rigid schedule, and plans may evolve based on community feedback and market requirements.

## Visual Roadmap

```mermaid
flowchart LR
    %% Stage 1: In Progress
    subgraph G1 ["In Progress (Launch Q2 2026)"]
        direction TB
        A[Spring Boot 4.0.6]
        B[Java 21 Support]
        C[Camunda 7.24]
    end

    %% Stage 2: Planned
    subgraph G2 ["Planned (Next)"]
        TBD1[To be defined]
    end

    %% Stage 3: Future Vision
    subgraph G3 ["Future Vision (Later)"]
        TBD2[To be defined]
    end

    %% Logical Flow connecting subgraphs directly
    G1 -.-> G2
    G2 -.-> G3

    %% Styling
    classDef inprogress fill:#e1f5fe,stroke:#01579b,stroke-width:2px;
    classDef planned fill:#fff,stroke:#333,stroke-width:1px,stroke-dasharray: 5 5;
    class A,B,C inprogress;
    class TBD1,TBD2 planned;
```

---

## In Progress (Launch Q2 2026)

This phase focuses on the upcoming major release, modernizing the core infrastructure of the platform.

*   **Java Migration:** Full support for **Java 21** (LTS).
*   **Spring Boot Upgrade:** Migration to **Spring Boot 4.0.6**.
*   **Camunda Engine Upgrade:** Official support for **Camunda 7.24**.

## Planned (Next)

*To be defined.*

## Future Vision (Later)

*To be defined.*
