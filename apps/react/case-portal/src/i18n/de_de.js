const defs = {
  general: {
    case: {
      status: {
        wip: 'In Bearbeitung',
        closed: 'Geschlossen',
        archived: 'Archiviert',
      },
    },
  },
  menu: {
    home: 'Übersicht',
    case: 'Fälle',
    task: 'Aufgaben',
    workspace: 'Arbeitsbereich',
    record: 'Datensätze',
    system: 'System',
    settings: 'Einstellungen',
    casebuilder: 'Fallgestaltung',
    documentation: 'Dokumentation',
    management: 'Verwaltung',
    lookAndFeel: 'Erscheinungsbild',

    profile: 'Profil',
    logout: 'Abmelden',
    language: 'Sprache',

    processes: 'Prozesse',
    caseDefinitions: 'Falldefinitionen',
    recordTypes: 'Datensatztypen',
    processEngines: 'Prozess-Engines',
    forms: 'Formulare',
    queues: 'Warteschlangen',

    externalLinks: 'Externe Links',
  },
  pages: {
    dashboard: {
      title: 'Mein Arbeitsbereich',
      cards: {
        wipcases: {
          label: 'In Bearbeitung',
        },
        caselist: {
          label: 'Alle',
        },
        tasklist: {
          label: 'Aufgaben',
        },
      },
    },
    caselist: {
      datagrid: {
        columns: {
          businesskey: 'Geschäftsschlüssel',
          statusdescription: 'Status',
          stage: 'Phase',
          createdat: 'Erstellt am',
          queue: 'Warteschlange',
          caseOwnerName: 'Eigentümer',
        },
        action: {
          details: 'Details',
        },
      },
      action: {
        newcase: 'Anlegen',
        refresh: 'Aktualisieren',
      },
    },
    caseform: {
      actions: {
        close: 'Schließen',
        reopen: 'Wieder öffnen',
        archive: 'Archivieren',
        newTask: 'Neue Aufgabe',
        startProcess: 'Prozess starten',
      },
      tabs: {
        details: 'Falldetails',
        tasks: 'Aufgaben',
        comments: 'Kommentare',
        attachments: 'Anhänge',
        emails: 'E-Mails',
        diagram: 'Diagramm',
      },
      diagram: {
        none: 'Für diesen Falltyp ist kein Diagramm hinterlegt.',
        currentStage: 'Aktuelle Phase',
        achieved: 'Meilenstein erreicht',
      },
      manualProcesses: {
        title: 'Zu startenden Prozess auswählen',
        noProcesses: 'Für diese Phase sind keine manuellen Prozesse verfügbar',
        startFailed: 'Der Prozess konnte nicht gestartet werden.',
      },
      validation: {
        pleaseCorrectErrors:
          'Bitte korrigieren Sie die folgenden Fehler im Formular:',
        requiredFieldsMissing:
          'Pflichtfelder fehlen oder enthalten ungültige Werte.',
        pleaseFixErrors:
          'Bitte beheben Sie die folgenden Fehler, bevor Sie fortfahren.',
      },
    },
    tasklist: {
      datagrid: {
        columns: {
          name: 'Aufgabe',
          caseinstanceid: 'Fall',
          processdefinitionid: 'Prozess',
          assignee: 'Bearbeiter',
          created: 'Erstellt',
          due: 'Fällig',
          followup: 'Wiedervorlage',
        },
        toolbar: {
          columns: 'Spalten',
          filters: 'Filter',
          density: 'Zeilenhöhe',
          export: 'Exportieren',
        },
      },
      newTask: {
        name: 'Aufgabe',
        description: 'Beschreibung',
        dueDate: 'Fälligkeitsdatum',
        assignee: 'Bearbeiter',
      },
      upcoming: 'Anstehend & überfällig',
    },
    taskform: {
      claim: 'Übernehmen',
      complete: 'Abschließen',
    },
    recordlist: {
      datagrid: {
        action: {
          details: 'Details',
        },
      },
      action: {
        newrecord: 'Neu',
      },
    },
    comments: {
      title: 'Kommentare',
      actions: {
        send: 'Senden',
        reply: 'Antworten',
        edit: {
          action: 'Bearbeiten',
          update: 'Aktualisieren',
          cancel: 'Abbrechen',
        },
        delete: 'Löschen',
      },
    },
    emails: {
      datagrid: {
        receivedDateTime: 'Empfangen',
        hasAttachments: 'Anhänge?',
        from: 'Von',
        to: 'An',
        bodyPreview: 'Vorschau',
        action: {
          compose: 'Neu',
        },
      },
      form: {
        title: 'Neue E-Mail',
        recipient: 'An',
        subject: 'Betreff',
        body: 'Nachricht',
        send: 'Senden',
      },
    },
    message: {
      fileUpload: {
        error: {
          couldNotUpload: 'Diese Datei konnte nicht hochgeladen werden.',
        },
      },
    },
    cmmnImport: {
      action: 'CMMN importieren',
      title: 'CMMN-Modell importieren',
      chooseFile: 'Datei auswählen',
      chooseDiagram: 'Diagramm auswählen (optional)',
      diagramHint:
        'Optional das Diagramm als SVG anhängen, um das Modell an jedem Fall anzuzeigen.',
      diagramRejected:
        'Der Falltyp wurde importiert, das Diagramm jedoch nicht gespeichert:',
      hint: 'Wählen Sie ein CMMN-1.1-Modell (.cmmn oder .xml). Eine gerenderte Grafik (SVG/PNG) kann nicht importiert werden.',
      import: 'Importieren',
      cancel: 'Abbrechen',
      done: 'Schließen',
      optional: 'optional',
      imported: '"{{name}}" wurde importiert.',
      summary: {
        stages: '{{count}} Phasen',
        tasks: '{{count}} Aufgaben',
        milestones: '{{count}} Meilensteine',
      },
      warnings: {
        title: '{{count}} Punkte konnten nicht exakt übernommen werden',
        intro:
          'CMMN kann mehr ausdrücken, als diese Plattform abbildet — bei einem Import bleibt daher immer etwas zurück. Bitte vor dem Importieren prüfen.',
      },
    },
    validation: {
      pleaseCorrectErrors:
        'Bitte korrigieren Sie die folgenden Fehler im Formular:',
      requiredFieldsMissing:
        'Pflichtfelder fehlen oder enthalten ungültige Werte.',
      requiredField: 'Dieses Feld ist ein Pflichtfeld.',
      maxLength: 'Die maximale Länge beträgt {{length}} Zeichen.',
      minLength: 'Die minimale Länge beträgt {{length}} Zeichen.',
      pattern: 'Ungültiges Format.',
      invalidEmail: 'Bitte geben Sie eine gültige E-Mail-Adresse ein.',
      minValue: 'Der Mindestwert ist {{min}}.',
      maxValue: 'Der Höchstwert ist {{max}}.',
      notANumber: 'Bitte geben Sie eine gültige Zahl ein.',
      invalidDate: 'Bitte geben Sie ein gültiges Datum ein.',
      invalidUrl: 'Bitte geben Sie eine gültige URL ein.',
      custom: 'Ungültiger Wert.',
      genericError:
        'Das Feld {{field}} enthält einen ungültigen Wert. Bitte prüfen und korrigieren.',
    },
  },
}

export default defs
