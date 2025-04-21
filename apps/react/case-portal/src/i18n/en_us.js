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
    workspace: 'Worlspace',
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

    externalLinks: 'External Links',
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
        refresh: 'Refresh',
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
      validation: {
        pleaseCorrectErrors: 'Please correct the following errors in the form:',
        requiredFieldsMissing:
          'Required fields are missing or contain invalid values.',
        pleaseFixErrors: 'Please fix the following errors before submitting.',
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
    validation: {
      pleaseCorrectErrors: 'Please correct the following errors in the form:',
      requiredFieldsMissing:
        'Required fields are missing or contain invalid values.',
      requiredField: 'This field is required.',
      maxLength: 'Maximum length is {{length}} characters.',
      minLength: 'Minimum length is {{length}} characters.',
      pattern: 'Invalid format.',
      invalidEmail: 'Please enter a valid email address.',
      minValue: 'Minimum value is {{min}}.',
      maxValue: 'Maximum value is {{max}}.',
      notANumber: 'Please enter a valid number.',
      invalidDate: 'Please enter a valid date.',
      invalidUrl: 'Please enter a valid URL.',
      custom: 'Invalid value.',
      genericError:
        'The {{field}} field has an invalid value. Please review and correct.',
    },
  },
}

export default defs
