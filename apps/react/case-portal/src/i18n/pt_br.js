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
    dataTable: 'Turnaround Table',
    dataTable2: 'Product Mix Entry',
    dataForm: 'WKS Form',
    dataForm2: 'WKS Form2',
    // Menu items for "Production / Norms Plan"
    productionNormsPlan: 'Plano de Produção / Normas',
    productMCUVal: 'Valor MCU Produto',
    maintenanceDetails: 'Maintenance Details',
    productDemand: 'Demanda de Produto',
    shutdownPlan: 'Plano de Parada',
    shutdownNorms: 'Shutdown Norms',
    normalOpNorms: 'Normal Operation Norms',
    slowdownPlan: 'Plano de Redução',
    taPlan: 'Plano de TA',
    feedStock: 'Plano de TA',
    productionNormsOutput: 'Normas de Produção (Saída)',
    consumptionNormsOutput: 'Normas de Consumo (Saída)',
    shutdownNormsOutput: 'Normas de Parada (Saída)',
    // New menu items for functions
    safety: 'Segurança', // New addition
    reliability: 'Confiabilidade', // New addition
    reports: 'Relatórios', // New addition
    contributionReport: 'Relatório de Contribuição', // New addition
    previousFYAOPResult: 'Resultado AOP do Ano Fiscal Anterior', // New addition
    matBalSheet: 'Balanço de MAT', // New addition
    workflow: 'Fluxo de Trabalho', // New addition
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
    // Pages for Production / Norms Plan
    productionNormsPlanPage: {
      title: 'Plano de Produção / Normas',
      subPages: {
        productMCUVal: 'Valor MCU Produto',
        maintenanceDetails: 'Maintenance Details',
        productDemand: 'Demanda de Produto',
        shutdownPlan: 'Plano de Parada',
        shutdownNorms: 'Shutdown Norms',
        normalOpNorms: 'Normal Operation Norms',
        slowdownPlan: 'Plano de Redução',
        taPlan: 'Plano de TA',
        productionNormsOutput: 'Normas de Produção (Saída)',
        consumptionNormsOutput: 'Normas de Consumo (Saída)',
        shutdownNormsOutput: 'Normas de Parada (Saída)',
        safety: 'Segurança', // New addition
        reliability: 'Confiabilidade', // New addition
        reports: 'Relatórios', // New addition
        contributionReport: 'Relatório de Contribuição', // New addition
        previousFYAOPResult: 'Resultado AOP do Ano Fiscal Anterior', // New addition
        matBalSheet: 'Balanço de MAT', // New addition
        workflow: 'Fluxo de Trabalho', // New addition
      },
    },
  },
}

export default defs
