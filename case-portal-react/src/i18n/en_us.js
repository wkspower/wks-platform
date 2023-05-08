const defs = {
    general: {
        case: {
            status: {
                wip: 'Work In Progress',
                closed: 'Closed',
                archived: 'Archived'
            }
        }
    },
    menu: {
        home: 'Dashboard',
        case: 'Cases',
        task: 'Tasks',
        record: 'Records',
        system: 'System',
        settings: 'Settings',
        caselifecicle: 'Case Life Cycle',
        documentation: 'Documentation',

        processes: 'Processes',
        caseDefinitions: 'Case Definitions',
        recordTypes: 'Record Types',
        processEngines: 'Process Engines',
        forms: 'Forms'
    },
    pages: {
        dashboard: {
            title: 'My Workspace',
            cards: {
                wipcases: {
                    label: 'Work in Progress'
                },
                caselist: {
                    label: 'All'
                },
                tasklist: {
                    label: 'Tasks'
                }
            }
        },
        caselist: {
            datagrid: {
                columns: {
                    businesskey: 'Business Key',
                    statusdescription: 'Status',
                    stage: 'Stage',
                    createdat: 'Created At',
                    caseOwnerName: 'Owner',
                },
                action: {
                    details: 'Details'
                }
            },
            action: {
                newcase: 'Create'
            }
        },
        caseform: {
            actions: {
                close: 'Close',
                reopen: 'Re-open',
                archive: 'Archive'
            },
            tabs: {
                details: 'Case Details',
                tasks: 'Tasks',
                comments: 'Comments',
                documents: 'Documents',
                emails: 'Emails'
            }
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
                    followup: 'Follow Up'
                },
                toolbar: {
                    columns: 'Columns',
                    filters: 'Filters',
                    density: 'Density',
                    export: 'Export'
                },
                action: {
                    details: 'Details'
                }
            }
        },
        taskform: {
            claim: 'Claim',
            complete: 'Complete'
        },
        recordlist: {
            datagrid: {
                action: {
                    details: 'Details'
                }
            },
            action: {
                newrecord: 'New'
            }
        },
        comments: {
            title: 'Comments',
            actions: {
                send: 'Send',
                reply: 'Reply',
                edit: {
                    action: 'Edit',
                    update: 'Update',
                    cancel: 'Candel'
                },
                delete: 'Delete'
            }
        },
        emails: {
            datagrid: {
                from: 'from',
                to: 'to',
                text: 'text'
            }
        },
        message: {
            fileUpload:{
                error :{
                    couldNotUpload: 'Could not upload this file.'
                }
            }
        }
    }
};

export default defs;
