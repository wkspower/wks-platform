// Tab ID to display name mapping
const TAB_ID_MAP = {
  '38290a23-71a7-4ef9-a9ca-034f6ca8d8c5': 'Unit Capacity',
  '10b99dcc-7bc7-4e61-802a-41bf651a2aaa': 'Shutdown',
  'cbb07481-0ded-42a1-a93a-1195d4467d16': 'Slowdown',
  '62abe351-a7bc-40a1-9941-bd4e604f6887': 'CPP Units SD Plan',
  '1f06147b-c931-4ef4-9e40-7b730026631c': 'PCG Outlook',
  'abfd738f-afca-4a88-864d-675624b9beb5': 'ROGC',
  'ccdf3861-ead3-42e7-82ea-0670b21c0ae2': 'Crude Blend Window',
}

// Column configurations for each tab
export const getColumnsForTab = (tabId, headerMap, valueFormat) => {
  const tabName = TAB_ID_MAP[tabId] || 'Unit Capacity'
  switch (tabName) {
    case 'Unit Capacity':
      return [
        {
          field: 'Particulars',
          title: 'Units',
          widthT: 120,
          locked: true,
          editable: true,
          disable: false,
          type: 'text',
          minWidth: 100,
        },
        {
          title: 'Capacity',
          children: [
            {
              field: 'uom',
              title: 'UOM',
              editable: true,
              type: 'text',
              minWidth: 100,
            },
            {
              field: 'kbpsd',
              title: 'KBPSD',
              editable: true,
              minWidth: 100,
            },
            {
              field: 'remarks',
              title: 'Remarks',
              minWidth: 200,
              editable: true,
            },
          ],
        },
      ]
    case 'Shutdown':
      return [
        {
          field: 'Particulars',
          title: 'Units',
          width: 150,
          locked: true,
          editable: true,
          type: 'text',
          minWidth: 100,
        },
        {
          title: 'Major Units Shutdown details',
          children: [
            {
              field: 'sdTotalDuration',
              title: 'SD Total duration in days',
              width: 180,
              editable: true,
              minWidth: 100,
            },
            {
              field: 'tentativeMonth',
              title: 'Start Date',
              width: 150,
              editable: true,
              minWidth: 100,
            },
            {
              field: 'purposeOfShutdown',
              title: 'Purpose of Shutdown',
              width: 200,
              editable: true,
              minWidth: 200,
            },
          ],
        },
      ]
    case 'Slowdown':
      return [
        {
          title: 'Major Units Slowdown details',
          children: [
            {
              field: 'Particulars',
              title: 'Units',
              width: 150,
              locked: true,
              editable: true,
              type: 'text',
              minWidth: 80,
            },
            {
              field: 'UOM',
              title: 'UOM',
              width: 150,
              minWidth: 80,
              locked: true,
              editable: true,
              type: 'text',
            },
            {
              field: 'tentativeDuration',
              title: 'Tentative Duration in days',
              width: 200,
              minWidth: 80,
              editable: true,
            },
            {
              field: 'throughputDuringSlowdown',
              title: 'Throughput during the Slowdown',
              width: 220,
              minWidth: 80,
              editable: true,
              type: 'text',
            },
            {
              field: 'tentativeMonth',
              title: 'Tentative Month',
              width: 150,
              minWidth: 80,
              editable: true,
            },
            {
              field: 'purposeOfSlowdown',
              title: 'Purpose of Slowdown',
              width: 200,
              minWidth: 80,
              editable: true,
            },
          ],
        },
      ]
    case 'CPP Units SD Plan':
      return [
        {
          title: "CPP Shutdown plan January'25 - March'26",
          children: [
            {
              field: 'Particulars',
              title: 'JMD-CPP',
              width: 120,
              minWidth: 80,
              editable: true,
              type: 'text',
            },
            {
              field: 'ibrDueDate',
              title: 'IBR Due date',
              width: 120,
              minWidth: 80,
              editable: true,
            },
            {
              title: 'GT maintenance',
              children: [
                {
                  field: 'gtMaintenance',
                  title: 'MI/HGPI/CI/Mods',
                  widthT: 240,
                  minWidth: 80,
                  editable: true,
                },
              ],
            },
            {
              field: 'noOfDays',
              title: 'No. of days',
              width: 100,
              minWidth: 80,
              editable: true,
            },
            {
              field: 'shutdownDate',
              title: 'Shutdown date',
              width: 120,
              minWidth: 80,
              editable: true,
            },
            {
              field: 'startupDate',
              title: 'Startup date',
              width: 120,
              minWidth: 80,
              editable: true,
            },
            {
              field: 'majorJobs',
              title: 'Major jobs',
              width: 180,
              minWidth: 80,
              editable: true,
              type: 'text',
            },
          ],
        },
      ]
    case 'PCG Outlook':
      return [
        {
          field: 'Particulars',
          title: 'Product',
          width: 150,
          editable: true,
        },
        { field: 'apr', title: headerMap[4], width: 70, editable: true },
        { field: 'may', title: headerMap[5], width: 70, editable: true },
        { field: 'june', title: headerMap[6], width: 70, editable: true },
        { field: 'july', title: headerMap[7], width: 70, editable: true },
        { field: 'aug', title: headerMap[8], width: 70, editable: true },
        { field: 'sep', title: headerMap[9], width: 70, editable: true },
        { field: 'oct', title: headerMap[10], width: 70, editable: true },
        { field: 'nov', title: headerMap[11], width: 70, editable: true },
        { field: 'dec', title: headerMap[12], width: 70, editable: true },
        { field: 'jan', title: headerMap[1], width: 70, editable: true },
        { field: 'feb', title: headerMap[2], width: 70, editable: true },
        { field: 'march', title: headerMap[3], width: 70, editable: true },
      ]
    case 'ROGC':
      return [
        {
          field: 'Particulars',
          title: 'Product',
          width: 150,
          editable: true,
          type: 'text',
        },
        { field: 'apr', title: headerMap[4], width: 70, editable: true, format: valueFormat },
        { field: 'may', title: headerMap[5], width: 70, editable: true, format: valueFormat },
        { field: 'june', title: headerMap[6], width: 70, editable: true, format: valueFormat },
        { field: 'july', title: headerMap[7], width: 70, editable: true, format: valueFormat },
        { field: 'aug', title: headerMap[8], width: 70, editable: true, format: valueFormat },
        { field: 'sep', title: headerMap[9], width: 70, editable: true, format: valueFormat },
        { field: 'oct', title: headerMap[10], width: 70, editable: true, format: valueFormat },
        { field: 'nov', title: headerMap[11], width: 70, editable: true, format: valueFormat },
        { field: 'dec', title: headerMap[12], width: 70, editable: true, format: valueFormat },
        { field: 'jan', title: headerMap[1], width: 70, editable: true, format: valueFormat },
        { field: 'feb', title: headerMap[2], width: 70, editable: true, format: valueFormat },
        { field: 'march', title: headerMap[3], width: 70, editable: true, format: valueFormat },
      ]
    case 'Crude Blend Window':
      return [
        {
          title: 'Crude Blend Window',
          children: [
            {
              field: 'property',
              title: 'Property',
              width: 180,
              editable: true,
            },
            { field: 'stream', title: 'Stream', width: 120, editable: true, type: 'text' },
            { field: 'unit', title: 'Unit', width: 80, editable: true, type: 'text' },
            { field: 'min', title: 'Min', width: 70, editable: true },
            { field: 'max', title: 'Max', width: 70, editable: true },
            {
              field: 'criticality',
              title: 'Criticality',
              width: 90,
              editable: true,
            },
            {
              field: 'remarks',
              title: 'Remarks',
              width: 400,
              editable: true,
            },
          ],
        },
      ]
    default:
      return []
  }
}

// Mock data for each tab
export const generateMockData = (tabId) => {
  const tabName = TAB_ID_MAP[tabId] || 'Unit Capacity'
  switch (tabName) {
    case 'Unit Capacity':
      return [
        {
          id: 1,
          Particulars: 'CDU1',
          uom: 'KBPSD',
          kbpsd: 345.0,
          remarks:
            'Unit capacity considered for min API of 27. L+N: CDU+ 7.4 KTPD max; CDU-2: 6.4 KTPD (Summer: March-Oct) & 7.4 KTPD max in winters (Nov-Feb)',
        },
        {
          id: 2,
          Particulars: 'CDU2',
          uom: 'KBPSD',
          kbpsd: 345.0,
          remarks:
            'PCD: Max 24.2 KTPD VR: Max 14.5 KTPD, however HDT VR to Coker will be 13.6 KTPD max',
        },
        {
          id: 3,
          Particulars: 'DHT1',
          uom: 'KBPSD',
          kbpsd: 80.0,
          remarks: 'Grade wise max. capacity : BS III: 100 kbpsd',
        },
        {
          id: 4,
          Particulars: 'DHT2',
          uom: 'KBPSD',
          kbpsd: 100.0,
          remarks: 'BS - VI: D1: 80 KBPSD, D2: 60 KBPSD',
        },
        {
          id: 5,
          Particulars: 'VGOHT1',
          uom: 'KBPSD',
          kbpsd: 104.5,
          remarks: '',
        },
        {
          id: 6,
          Particulars: 'VGOHT2',
          uom: 'KBPSD',
          kbpsd: 104.5,
          remarks: '',
        },
        {
          id: 7,
          Particulars: 'FCCU',
          uom: 'KBPSD',
          kbpsd: 215,
          remarks: '',
        },
        {
          id: 26,
          Particulars: 'HPIB',
          uom: 'TPD',
          kbpsd: '1. HPIB : 409.5 TPD',
          remarks: '2. Butene-1 : 195 TPD at 130% capacity; 3. MTBE: 816 TPD',
        },
      ]
    case 'Shutdown':
      return [
        {
          id: 1,
          Particulars: 'CDU#1',
          sdTotalDuration: 28.0,
          tentativeMonth: '05-Jan-25',
          purposeOfShutdown:
            'M&I Jobs / Vacuum Column Bed Replacement/ Heater tube pigging and replacement, APH replacement',
        },
        {
          id: 2,
          Particulars: 'Sat LPG Merox-331',
          sdTotalDuration: 15.0,
          tentativeMonth: '05-Jan-25',
          purposeOfShutdown:
            'M&I and LPG/Amine OLS Normalization along with CDU',
        },
        {
          id: 3,
          Particulars: 'KMU1',
          sdTotalDuration: 18.0,
          tentativeMonth: '05-Jan-25',
          purposeOfShutdown:
            'with CDU-1 for Reactor charcoal replacement and M&I',
        },
        {
          id: 4,
          Particulars: 'KMU2',
          sdTotalDuration: 18.0,
          tentativeMonth: '20-Jan-25',
          purposeOfShutdown:
            'with CDU-1 for Reactor charcoal replacement and M&I',
        },
        {
          id: 5,
          Particulars: 'CBA-3',
          sdTotalDuration: 31.0,
          tentativeMonth: '05-Jan-25',
          purposeOfShutdown:
            'R01/ R02/R03 catalyst replacement with new catalyst, WHBs tube cleaning & IBR inspection, HP steam line bootleg valve and trap replacement job, pit inspection',
        },
        {
          id: 6,
          Particulars: 'SWS-3 With CDU-1 S/D',
          sdTotalDuration: 12.0,
          tentativeMonth: '05-Jan-25',
          purposeOfShutdown:
            'Feed Excahnger and reboilers tune cleaning and inspection.',
        },
        {
          id: 7,
          Particulars: 'SWS-4 With CDU-1 S/D',
          sdTotalDuration: 12.0,
          tentativeMonth: '17-Jan-25',
          purposeOfShutdown:
            'Feed Excahnger and reboilers tune cleaning and inspection.',
        },
        {
          id: 8,
          Particulars: 'PP Line-C',
          sdTotalDuration: 16.0,
          tentativeMonth: '05-Jan-25',
          purposeOfShutdown:
            'Reactor cleaning, Extruder overhauling (with FCC-2 slowdown)',
        },
        {
          id: 9,
          Particulars: 'PP Line-B',
          sdTotalDuration: 16.0,
          tentativeMonth: '05-Jun-25',
          purposeOfShutdown:
            'Line B Cycle gas compressor overhauling and Reactor inspection & cleaning',
        },
        {
          id: 10,
          Particulars: 'HPIB',
          sdTotalDuration: 27.0,
          tentativeMonth: '01-Nov-25',
          purposeOfShutdown:
            'MTBE secondary Reactor Catalyst replacement and M&I of HPC and other equipment',
        },
      ]
    case 'Slowdown':
      return [
        {
          id: 1,
          Particulars: 'Coker-1',
          tentativeDuration: 11.0,
          throughputDuringSlowdown: '170 KBPSD',
          tentativeMonth: '2025-03-25',
          purposeOfSlowdown: 'Heater 1,2,3,4 Pigging',
        },
        {
          id: 2,
          Particulars: 'Coker-1',
          tentativeDuration: 11.0,
          throughputDuringSlowdown: '170 KBPSD',
          tentativeMonth: '2025-08-25',
          purposeOfSlowdown: 'Heater 1,2,3,4 Pigging',
        },
        {
          id: 3,
          Particulars: 'Coker-1',
          tentativeDuration: 6.0,
          throughputDuringSlowdown: '160 KBPSD',
          tentativeMonth: '2025-08-25',
          purposeOfSlowdown: 'Heater 5 Pigging',
        },
        {
          id: 4,
          Particulars: 'Coker-1',
          tentativeDuration: 8.0,
          throughputDuringSlowdown: '206 KBPSD',
          tentativeMonth: '2025-02-25',
          purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
        },
        {
          id: 5,
          Particulars: 'Coker-1',
          tentativeDuration: 8.0,
          throughputDuringSlowdown: '206 KBPSD',
          tentativeMonth: '2025-04-25',
          purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
        },
        {
          id: 6,
          Particulars: 'Coker-1',
          tentativeDuration: 8.0,
          throughputDuringSlowdown: '206 KBPSD',
          tentativeMonth: '2025-06-25',
          purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
        },
        {
          id: 7,
          Particulars: 'Coker-1',
          tentativeDuration: 8.0,
          throughputDuringSlowdown: '206 KBPSD',
          tentativeMonth: '2025-10-25',
          purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
        },
      ]
    case 'CPP Units SD Plan':
      return [
        {
          id: 1,
          Particulars: 'GT-1/HRSG-1',
          ibrDueDate: '2025-01-23',
          gtMaintenance: ['MI', 'HGPI'],
          noOfDays: 66,
          shutdownDate: '2024-12-24',
          startupDate: '2025-02-28',
          majorJobs: 'HRSG Economiser & Side walls replacement',
        },
        {
          id: 2,
          Particulars: 'GT-6/HRSG-6',
          ibrDueDate: '2025-03-07',
          gtMaintenance: ['HGPI'],
          noOfDays: 70,
          shutdownDate: '2025-03-01',
          startupDate: '2025-05-10',
          majorJobs: 'Rotor replacement, HRSG side walls, 70 days',
        },
        {
          id: 3,
          Particulars: 'GT-13/HRSG-13',
          ibrDueDate: '2025-03-14',
          gtMaintenance: ['MI'],
          noOfDays: 11,
          shutdownDate: '2025-03-05',
          startupDate: '2025-03-16',
          majorJobs: 'HRSG side walls replacement',
        },
        {
          id: 4,
          Particulars: 'GT-4/HRSG-4',
          ibrDueDate: '2025-05-13',
          gtMaintenance: ['HGPI'],
          noOfDays: 30,
          shutdownDate: '2025-03-17',
          startupDate: '2025-04-16',
          majorJobs: 'Turbine rotor replacement',
        },
      ]
    case 'PCG Outlook':
      return [
        {
          id: 1,
          srno: 1,
          Particulars: 'Gasifier Availability',
          jan: 2.9,
          feb: 2.8,
          march: 2.9,
          apr: 2.8,
          may: 2.9,
          june: 2.8,
          july: 2.9,
          aug: 2.8,
          sep: 2.9,
          oct: 2.8,
          nov: 2.9,
          dec: 2.8,
        },
        {
          id: 2,
          srno: 2,
          Particulars: 'SynGas Production',
          jan: 6.0,
          feb: 6.0,
          march: 6.0,
          apr: 6.0,
          may: 6.0,
          june: 6.0,
          july: 6.0,
          aug: 6.0,
          sep: 6.0,
          oct: 6.0,
          nov: 6.0,
          dec: 6.0,
        },
      ]
    case 'ROGC':
      return [
        {
          id: 1,
          srno: 1,
          Particulars: 'F1',
          jan: 18,
          feb: 0.0,
          march: 0.0,
          apr: 0.0,
          may: 2.3,
          june: 0.0,
          july: 0.0,
          aug: 2.3,
          sep: 0.0,
          oct: 0.0,
          nov: 0.0,
          dec: 2.3,
        },
        {
          id: 2,
          srno: 2,
          Particulars: 'F2',
          jan: 2.3,
          feb: 0.0,
          march: 0.0,
          apr: 0.0,
          may: 0.0,
          june: 0.0,
          july: 2.3,
          aug: 0.0,
          sep: 0.0,
          oct: 2.3,
          nov: 0.0,
          dec: 0.0,
        },
        {
          id: 3,
          srno: 3,
          Particulars: 'F3',
          jan: 0.0,
          feb: 0.0,
          march: 0.0,
          apr: 2.3,
          may: 0.0,
          june: 0.0,
          july: 2.3,
          aug: 0.0,
          sep: 0.0,
          oct: 2.3,
          nov: 0.0,
          dec: 0.0,
        },
        {
          id: 4,
          srno: 4,
          Particulars: 'F4',
          jan: 0.0,
          feb: 2.3,
          march: 0.0,
          apr: 2.3,
          may: 0.0,
          june: 0.0,
          july: 0.0,
          aug: 0.0,
          sep: 0.0,
          oct: 0.0,
          nov: 2.3,
          dec: 0.0,
        },
        {
          id: 5,
          srno: 5,
          Particulars: 'F5',
          jan: 2.3,
          feb: 0.0,
          march: 0.0,
          apr: 0.0,
          may: 2.3,
          june: 0.0,
          july: 0.0,
          aug: 2.3,
          sep: 0.0,
          oct: 2.3,
          nov: 0.0,
          dec: 2.3,
        },
        {
          id: 6,
          srno: 6,
          Particulars: 'F6',
          jan: 0.0,
          feb: 0.0,
          march: 2.3,
          apr: 0.0,
          may: 0.0,
          june: 2.3,
          july: 0.0,
          aug: 0.0,
          sep: 0.0,
          oct: 0.0,
          nov: 0.0,
          dec: 0.0,
        },
        {
          id: 7,
          srno: 7,
          Particulars: 'Demo',
          jan: 2.3,
          feb: 2.3,
          march: 0.0,
          apr: 2.3,
          may: 2.3,
          june: 0.0,
          july: 2.3,
          aug: 2.3,
          sep: 0.0,
          oct: 2.3,
          nov: 0.0,
          dec: 2.3,
        },
      ]
    case 'Crude Blend Window':
      return [
        {
          id: 1,
          no: '1.1',
          property: 'API',
          stream: 'CDU feed',
          unit: 'degree',
          min: 26.0,
          max: '-',
          criticality: 2.0,
          remarks:
            'Max acceptable API delta in successive crude blends change is 2 . For 330 KBPSD min API is 26 & for 345 KBPSD min API is 27.5',
        },
        {
          id: 2,
          no: '1.2',
          property: 'TAN',
          stream: 'CDU feed',
          unit: 'mg KOH/gm',
          min: 1.3,
          max: '',
          criticality: 1.0,
          remarks:
            'Upper TAN to be targeted for 1.2 + 0.1 margin of PIMS error',
        },
        {
          id: 3,
          no: '1.3',
          property: 'Sulfur',
          stream: 'CDU feed',
          unit: 'Wt%',
          min: 1.1,
          max: 2.7,
          criticality: 1,
          remarks:
            "1. Lower limit is based on sulphur/TAN ratio with High TAN (>0.8) crude blend processing and CBA capacity.\n2. Considering AGTL Design for 2.7 WT%S @45.8 KTPD/326 KBPSD crude T'put. At Higher T'put of 330/335/340/345 Max S is limited at 2.66/2.63/2.59/2.55 WT%.",
        },
        {
          id: 4,
          no: '1.4',
          property: 'K. Visc. @40°C',
          stream: 'CDU feed',
          unit: 'cSt',
          min: '',
          max: 27,
          criticality: 2,
          remarks:
            'Max limit:- for Desalter performance. (High viscosity impacts performance adversely).\nDTA RTF crude charge pumps are designed to handle max viscosity 25 cSt crude blend.',
        },
        {
          id: 5,
          no: '1.5',
          property: 'Asp to Resin ratio',
          stream: 'CDU feed',
          unit: '',
          min: '',
          max: 0.35,
          criticality: 2,
          remarks:
            'While blending the Crudes having high Saturates (>50%), it is proposed to ensure the blend Colloidal Instability Index (CII) not to cross 1.0 and blend Asphaltene to Resin ratio remains less than 0.35.',
        },
        {
          id: 6,
          no: '1.6',
          property: 'BS&W',
          stream: 'CDU feed',
          unit: 'vol %',
          min: '',
          max: 1.0,
          criticality: 1.0,
          remarks:
            'This parameter is critical to ensure smooth desalter performance & unit reliability.',
        },
        {
          id: 7,
          no: '1.7',
          property: 'Salts',
          stream: 'CDU feed',
          unit: 'ptb',
          min: '',
          max: 70.0,
          criticality: 1.0,
          remarks:
            'This parameter is critical to ensure smooth desalter performance & unit reliability.',
        },
      ]
    default:
      return []
  }
}
