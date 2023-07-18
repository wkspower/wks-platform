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
        record: 'Registros',
        system: 'Sistema',
        settings: 'Configurações',
        caselifecicle: 'Ciclo de vida do caso',
        documentation: 'Documentação',

        processes: 'Processos',
        caseDefinitions: 'Definições de Casos',
        recordTypes: 'Tipos de Registros',
        processEngines: 'Motores de Processos',
        forms: 'Formulários',
        queues: 'Filas'
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
                    businesskey: 'Número',
                    statusdescription: 'Situação',
                    stage: 'Estágio',
                    createdat: 'Criado em',
                    caseOwnerName: 'Criado por' 
                },
                action: {
                    details: 'Detalhar'
                }
            },
            action: {
                newcase: 'Criar'
            }
        },
        caseform: {
            actions: {
                close: 'Encerrar',
                reopen: 'Reabrir',
                archive: 'Arquivar',
                newTask: 'Nova Tarefa'
            },
            tabs: {
                details: 'Detalhes do Caso',
                tasks: 'Tarefas',
                comments: 'Comentários',
                documents: 'Documentos',
                emails: 'Emails'
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
            },
            newTask:{
                name: 'Tarefa',
                description: 'Descrição',
                dueDate: 'Prazo',
                assignee: 'Responsável'
            }

        },
        taskform: {
            claim: 'Bloquear',
            complete: 'Finalizar'
        },
        recordlist: {
            datagrid: {
                action: {
                    details: 'Detalhar'
                }
            },
            action: {
                newrecord: 'Novo'
            }
        },
        comments: {
            title: 'Comentários',
            actions: {
                send: 'Enviar',
                reply: 'Responder',
                edit: {
                    action: 'Editar',
                    update: 'Alterar',
                    cancel: 'Cancelar'
                },
                delete: 'Remover'
            }
        },
        emails: {
            datagrid: {
                from: 'De',
                to: 'Para',
                text: 'Texto'
            }
        },
        message: {
            fileUpload:{
                error :{
                    couldNotUpload: 'Não foi possível fazer o upload do arquivo.'
                }
            }
        }
    }
};

export default defs;
