const defs = {
  general: {
    case: {
      status: {
        wip: 'Work In Progress',
        closed: 'Closed',
        archived: 'Archived',
      },
    },
  },
  menu: {
    home: 'Dashboard',
    case: 'Cases',
    task: 'Tasks',
    record: 'Records',
    system: 'System',
    settings: 'Settings',
    casebuilder: 'Case Builder',
    documentation: 'Documentation',

    processes: 'Processes',
    caseDefinitions: 'Case Definitions',
    recordTypes: 'Record Types',
    processEngines: 'Process Engines',
    forms: 'Forms',
    queues: 'Queues',
    dataTable: 'Turnaround Table',
    dataTable2: 'Product Mix Entry',
    dataForm: 'WKS Form',
    dataForm2: 'WKS Form2',
    // New menu items for "Production / Norms Plan"
    productionNormsPlan: 'Production / Norms Plan',
    productMCUVal: 'Production Volume Data',
    maintenanceDetails: 'Maintenance Details',
    consumptionNorms: 'Consumption AOP',
    productionNorms: 'Production AOP',
    catalystSelectivity: 'Configuration',
    productDemand: 'Business Demand',
    shutdownPlan: 'Shutdown/TA Activities',
    shutdownNorms: 'Shutdown Norms',
    slowdownNorms: 'Slowdown Norms',
    normalOpNorms: 'Normal Operation Norms',
    slowdownPlan: 'Slowdown Activities',
    taPlan: 'Turnaround Activities',
    feedStock: 'Feed Stock Availability',

    productionNormsOutput: 'Production Norms (Output)',
    consumptionNormsOutput: 'Consumption Norms (Output)',
    shutdownNormsOutput: 'Shutdown Norms (Output)',
    // Additional entries for functions
    safety: 'Safety', // New addition
    functions: 'Functions', // New addition
    reliability: 'Reliability', // New addition
    reports: 'Reports', // New addition
    annualAopCostReport: 'Annual AOP Cost Basis', // New addition
    productionVolumeDataBasis: 'Production Volume Data Basis', // New addition
    plantsProduction: 'Plants Production Summary', // New addition
    monthwise: 'Monthwise Production Plan',
    monthwiseRawMaterial: 'Monthwise Raw Material',
    previousFYAOPResult: 'Previous FY AOP Result', // New addition
    matBalSheet: 'MAT Bal Sheet', // New addition
    workflow: 'AOP Approval Flow', // New addition
    userManage: 'User Management', // New addition
  },
  pages: {
    dashboard: {
      title: 'My Workspace',
      cards: {
        wipcases: {
          label: 'Work in Progress',
        },
        caselist: {
          label: 'All',
        },
        tasklist: {
          label: 'Tasks',
        },
      },
    },
    caselist: {
      datagrid: {
        columns: {
          businesskey: 'Business Key',
          statusdescription: 'Status',
          stage: 'Stage',
          createdat: 'Created At',
          queue: 'Queue',
          caseOwnerName: 'Owner',
        },
        action: {
          details: 'Details',
        },
      },
      action: {
        newcase: 'Create',
      },
    },
    caseform: {
      actions: {
        close: 'Close',
        reopen: 'Re-open',
        archive: 'Archive',
        newTask: 'New Task',
        startProcess: 'Start Process',
      },
      tabs: {
        details: 'Case Details',
        tasks: 'Tasks',
        comments: 'Comments',
        attachments: 'Attachments',
        emails: 'Emails',
      },
      manualProcesses: {
        title: 'Choose a process to start',
      },
    },
    tasklist: {
      datagrid: {
        columns: {
          name: 'Task',
          caseinstanceid: 'Case',
          processdefinitionid: 'Process',
          assignee: 'Assignee',
          created: 'Created',
          due: 'Due',
          followup: 'Follow Up',
        },
        toolbar: {
          columns: 'Columns',
          filters: 'Filters',
          density: 'Density',
          export: 'Export',
        },
      },
      newTask: {
        name: 'Task',
        description: 'Description',
        dueDate: 'Due Date',
        assignee: 'Assignee',
      },
      upcoming: 'Upcoming & Overdue',
    },
    taskform: {
      claim: 'Claim',
      complete: 'Complete',
    },
    recordlist: {
      datagrid: {
        action: {
          details: 'Details',
        },
      },
      action: {
        newrecord: 'New',
      },
    },
    comments: {
      title: 'Comments',
      actions: {
        send: 'Send',
        reply: 'Reply',
        edit: {
          action: 'Edit',
          update: 'Update',
          cancel: 'Candel',
        },
        delete: 'Delete',
      },
    },
    emails: {
      datagrid: {
        receivedDateTime: 'Received',
        hasAttachments: 'Attachments?',
        from: 'From',
        to: 'To',
        bodyPreview: 'Preview',
        action: {
          compose: 'New',
        },
      },
      form: {
        title: 'New e-mail',
        recipient: 'To',
        subject: 'Subject',
        body: 'Body',
        send: 'Send',
      },
    },
    message: {
      fileUpload: {
        error: {
          couldNotUpload: 'Could not upload this file.',
        },
      },
    },
    productionNormsPlanPage: {
      title: 'Production / Norms Plan',
      subPages: {
        productMCUVal: 'Production Volume Data',
        maintenanceDetails: 'Maintenance Details',
        consumptionNorms: 'Consumption AOP',
        productionNorms: 'Production AOP',
        catalystSelectivity: 'Configuration',
        productDemand: 'Business Demand',
        shutdownPlan: 'Shutdown Activities',
        shutdownNorms: 'Shutdown Norms',
        normalOpNorms: 'Normal Operation Norms',
        slowdownPlan: 'Slowdown Activities',
        taPlan: 'Turnaround Activities',
        productionNormsOutput: 'Production Norms (Output)',
        consumptionNormsOutput: 'Consumption Norms (Output)',
        shutdownNormsOutput: 'Shutdown Norms (Output)',
        safety: 'Safety', // New addition
        reliability: 'Reliability', // New addition
        reports: 'Reports', // New addition
        annualAopCostReport: 'Annual AOP Cost Basis', // New addition
        productionVolumeDataBasis: 'Production Volume Data Basis', // New addition
        plantsProduction: 'Plants Production Summary',
        monthwise: 'Monthwise Production Plan',
        monthwiseRawMaterial: 'Monthwise Raw Material',
        previousFYAOPResult: 'Previous FY AOP Result', // New addition
        matBalSheet: 'MAT Bal Sheet', // New addition
        userManage: 'User Management', // New addition
        workflow: 'AOP Approval Flow', // New addition
      },
    },
  },
}

export default defs
