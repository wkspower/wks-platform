export default {
    en: {
        translation: {
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
                case: 'Generic Case',
                task: 'Tasks',
                record: 'Records'
            },
            submenu: {
                case: {
                    '1-motion-detected-form': 'Generic Case',
                    '2-motion-detected-form': 'Motion Detected',
                    'contractor-onboarding-contractor-on-boarding-form': 'Contractor On Boarding Case'
                },
                record: {
                    client: 'Client',
                    contractor: 'Contractor'
                }
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
                            createdat: 'Created At'
                        },
                        action: {
                            details: 'Details'
                        }
                    },
                    action: {
                        newcase: 'New Case'
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
                recordlist: {
                    datagrid: {
                        columns: {
                            name: 'Name',
                            address: 'Address',
                            country: 'Country'
                        },
                        action: {
                            details: 'Details'
                        }
                    },
                    action: {
                        newrecord: 'New'
                    }
                }
            }
        }
    },
    ptBR: {
        translation: {
            general: {
                case: {
                    status: {
                        wip: 'Em Andamento',
                        closed: 'Fechado',
                        archived: 'Arquivado'
                    }
                }
            },
            menu: {
                home: 'Dashboard',
                case: 'Casos',
                task: 'Tarefas',
                record: 'Registros'
            },
            submenu: {
                case: {
                    '1-motion-detected-form': 'Genérico',
                    '2-motion-detected-form': 'Movimento Detectado',
                    'contractor-onboarding-contractor-on-boarding-form': 'Contrato Embarcado'
                },
                record: {
                    client: 'Cliente',
                    contractor: 'Contratante'
                }
            },
            pages: {
                dashboard: {
                    title: 'Área de trabalho',
                    cards: {
                        wipcases: {
                            label: 'Em Andamento'
                        },
                        caselist: {
                            label: 'Tudo'
                        },
                        tasklist: {
                            label: 'Tarefas'
                        }
                    }
                },
                caselist: {
                    datagrid: {
                        columns: {
                            businesskey: 'ID',
                            statusdescription: 'Situação',
                            stage: 'Estágio',
                            createdat: 'Criado em'
                        },
                        action: {
                            details: 'Detalhar'
                        }
                    },
                    action: {
                        newcase: 'Novo Caso'
                    }
                },
                tasklist: {
                    datagrid: {
                        columns: {
                            name: 'Tarefa',
                            caseinstanceid: 'Caso',
                            processdefinitionid: 'Processo',
                            assignee: 'Associado',
                            created: 'Criado em',
                            due: 'Concluído em',
                            followup: 'Acompanhar'
                        },
                        toolbar: {
                            columns: 'Colunas',
                            filters: 'Filtros',
                            density: 'Densidade',
                            export: 'Exportação'
                        },
                        action: {
                            details: 'Detalhar'
                        }
                    }
                },
                recordlist: {
                    datagrid: {
                        columns: {
                            name: 'Nome',
                            address: 'Endereço',
                            country: 'País'
                        },
                        action: {
                            details: 'Detalhar'
                        }
                    },
                    action: {
                        newrecord: 'Novo'
                    }
                }
            }
        }
    }
};
