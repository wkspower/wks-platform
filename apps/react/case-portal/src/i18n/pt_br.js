const defs = {
  general: {
    case: {
      status: {
        wip: 'Em Andamento',
        closed: 'Fechado',
        archived: 'Arquivado',
      },
    },
  },
  menu: {
    home: 'Dashboard',
    case: 'Casos',
    task: 'Tarefas',
    workspace: 'Area de Trabalho',
    record: 'Registros',
    system: 'Sistema',
    settings: 'Configurações',
    casebuilder: 'Case Builder',
    documentation: 'Documentação',

    processes: 'Processos',
    caseDefinitions: 'Definições de Casos',
    recordTypes: 'Tipos de Registros',
    processEngines: 'Motores de Processos',
    forms: 'Formulários',
    queues: 'Filas',

    externalLinks: 'Links Externos',
  },
  pages: {
    dashboard: {
      title: 'Área de trabalho',
      cards: {
        wipcases: {
          label: 'Em Andamento',
        },
        caselist: {
          label: 'Tudo',
        },
        tasklist: {
          label: 'Tarefas',
        },
      },
    },
    caselist: {
      datagrid: {
        columns: {
          businesskey: 'Número',
          statusdescription: 'Situação',
          stage: 'Estágio',
          createdat: 'Criado em',
          queue: 'Fila',
          caseOwnerName: 'Criado por',
        },
        action: {
          details: 'Detalhar',
        },
      },
      action: {
        newcase: 'Criar',
        refresh: 'atualizar',
      },
    },
    caseform: {
      actions: {
        close: 'Encerrar',
        reopen: 'Reabrir',
        archive: 'Arquivar',
        newTask: 'Nova Tarefa',
        startProcess: 'Iniciar Processo',
      },
      tabs: {
        details: 'Detalhes do Caso',
        tasks: 'Tarefas',
        comments: 'Comentários',
        attachments: 'Anexos',
        emails: 'Emails',
      },
      manualProcesses: {
        title: 'Selecione o processo para iniciar',
      },
      validation: {
        pleaseCorrectErrors:
          'Por favor, corrija os seguintes erros no formulário:',
        requiredFieldsMissing:
          'Campos obrigatórios estão ausentes ou contêm valores inválidos.',
        pleaseFixErrors:
          'Por favor, corrija os seguintes erros antes de enviar.',
      },
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
          followup: 'Acompanhar',
        },
        toolbar: {
          columns: 'Colunas',
          filters: 'Filtros',
          density: 'Densidade',
          export: 'Exportação',
        },
      },
      newTask: {
        name: 'Tarefa',
        description: 'Descrição',
        dueDate: 'Prazo',
        assignee: 'Responsável',
      },
      upcoming: 'Futuras e Atrasadas',
    },
    taskform: {
      claim: 'Bloquear',
      complete: 'Finalizar',
    },
    recordlist: {
      datagrid: {
        action: {
          details: 'Detalhar',
        },
      },
      action: {
        newrecord: 'Novo',
      },
    },
    comments: {
      title: 'Comentários',
      actions: {
        send: 'Enviar',
        reply: 'Responder',
        edit: {
          action: 'Editar',
          update: 'Alterar',
          cancel: 'Cancelar',
        },
        delete: 'Remover',
      },
    },
    emails: {
      datagrid: {
        receivedDateTime: 'Recebido',
        hasAttachments: 'Anexos?',
        from: 'De',
        to: 'Para',
        bodyPreview: 'Prévia',
        action: {
          compose: 'Novo',
        },
      },
      form: {
        title: 'Novo E-mail',
        recipient: 'Para',
        subject: 'Assunto',
        body: 'Corpo',
        send: 'Enviar',
      },
    },
    message: {
      fileUpload: {
        error: {
          couldNotUpload: 'Não foi possível fazer o upload do arquivo.',
        },
      },
    },
    validation: {
      pleaseCorrectErrors:
        'Por favor, corrija os seguintes erros no formulário:',
      requiredFieldsMissing:
        'Campos obrigatórios estão ausentes ou contêm valores inválidos.',
      requiredField: 'Este campo é obrigatório.',
      maxLength: 'O comprimento máximo é de {{length}} caracteres.',
      minLength: 'O comprimento mínimo é de {{length}} caracteres.',
      pattern: 'Formato inválido.',
      invalidEmail: 'Por favor, insira um endereço de email válido.',
      minValue: 'O valor mínimo é {{min}}.',
      maxValue: 'O valor máximo é {{max}}.',
      notANumber: 'Por favor, insira um número válido.',
      invalidDate: 'Por favor, insira uma data válida.',
      invalidUrl: 'Por favor, insira uma URL válida.',
      custom: 'Valor inválido.',
      genericError:
        'O campo {{field}} contém um valor inválido. Por favor, revise e corrija.',
    },
  },
}

export default defs
