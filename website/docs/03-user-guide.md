---
id: user-guide
title: User guide
---

## Management Console
The Management Console is a web application that comprises all the configurations of the system.

## Case Definition
A Case Definition is the 'template' for the creation of new Cases Instances. It defines which attributes, stages and processes definitions will be used by Cases Instances created from it.
Cases definitions configurations are splitted in the following sections:

### General
These are the general information of a case:
- ID: the case definition unique identificator
- Name: the case definition name
- BPM Engine: the BPM engine where processes related to this case will be running
- Process Definition: The process that will orchestrate the life-cycle of cases created from this Case Definition
- Form: the form used to kick-off cases from this Case Definition

### Stages
Cases lifecycles spans over its stages. The case stages are defined in a case definition. The following are stages examples in what could be a "Contract Creation Case":

- Data Collection
- Data Analysis
- Data Approval
- Contract writing
- Contract signing

### Kanban
Configurations from the Kanban section will define how cards will be displayed in the Case Kanban view in the Users Workspace.
There are two fields for this configuration:
- **Title** (comma separated): if provided, these attributes values will be displayed for each Case Card **Tile** in the Kanban View  
- **Content** (comma separated): if provided, these attributes values will be displayed for each Case Card **Content** in the Kanban View.