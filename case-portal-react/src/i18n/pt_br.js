const defs = {
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
};

export default defs;
