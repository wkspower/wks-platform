export const MockReportService = {
  async getReport({ category }) {
    // You can log or inspect plantId/year if you need
    switch (category) {
      case 'ProductMix':
        return {
          columns: [
            { field: 'sno', headerName: 'SL.No', align: 'right' },
            { field: 'productName', headerName: 'Product name', flex: 2 },
            { field: 'unit', headerName: 'Unit' },
            { field: 'rsPerMt', headerName: 'Rs/MT', flex: 2, align: 'right' },
            {
              field: 'prodBudget',
              headerName: 'Budget',
              flex: 2,
              align: 'right',
            },
            {
              field: 'prodActual',
              headerName: 'Actual',
              flex: 2,
              align: 'right',
            },
            {
              field: 'prodNext',
              headerName: 'Forecast',
              flex: 2,
              align: 'right',
            },
          ],
          columnGrouping: [
            {
              groupId: 'costing',
              headerName: 'Costing',
              children: [{ field: 'rsPerMt' }],
            },
            {
              groupId: 'production',
              headerName: 'Production, MT',
              children: [
                {
                  groupId: 'current',
                  headerName: 'Current',
                  children: [{ field: 'prodBudget' }, { field: 'prodActual' }],
                },
                {
                  groupId: 'forecast',
                  headerName: 'Forecast',
                  children: [{ field: 'prodNext' }],
                },
              ],
            },
          ],
          rows: [
            {
              id: 1,
              sno: 1,
              productName: 'EOE',
              unit: 'MT',
              rsPerMt: 25000,
              prodBudget: 942,
              prodActual: 783,
              prodNext: 895,
            },
            {
              id: 2,
              sno: 2,
              productName: 'MEG',
              unit: 'MT',
              rsPerMt: 22000,
              prodBudget: 723,
              prodActual: 631,
              prodNext: 804,
            },
            {
              id: 3,
              sno: 3,
              productName: 'EO',
              unit: 'MT',
              rsPerMt: 23000,
              prodBudget: 657,
              prodActual: 523,
              prodNext: 642,
            },
            {
              id: 4,
              sno: 4,
              productName: 'NSR',
              unit: 'Rs/MT',
              rsPerMt: null,
              prodBudget: 2112,
              prodActual: 2056,
              prodNext: 2190,
            },
          ],
        }

      case 'ByProducts':
        return {
          columns: [
            { field: 'sno', headerName: 'SL.No', align: 'right', flex: 0.5 },
            { field: 'byProductName', headerName: 'By product name', flex: 2 },
            { field: 'unit', headerName: 'Unit', flex: 1 },
            {
              field: 'priceRsMt',
              headerName: 'Rs/MT',
              flex: 1,
              align: 'right',
            },
            {
              field: 'normBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'normActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'normForecast',
              headerName: 'Forecast',
              flex: 1,
              align: 'right',
            },
            {
              field: 'costBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'costActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'costForecast',
              headerName: 'Forecast',
              flex: 1,
              align: 'right',
            },
          ],
          columnGrouping: [
            {
              groupId: 'price',
              headerName: 'Price',
              children: [{ field: 'priceRsMt' }],
            },
            {
              groupId: 'norm',
              headerName: 'Norm Unit/MT',
              children: [
                {
                  groupId: 'normDtl',
                  headerName: 'Norm Details',
                  children: [{ field: 'normBudget' }, { field: 'normActual' }],
                },
                {
                  groupId: 'normDtl2',
                  headerName: 'Norm Forecast',
                  children: [{ field: 'normForecast' }],
                },
              ],
            },
            {
              groupId: 'cost',
              headerName: 'Cost Rs/MT',
              children: [
                {
                  groupId: 'costDtl',
                  headerName: 'Cost Details',
                  children: [{ field: 'costBudget' }, { field: 'costActual' }],
                },
                {
                  groupId: 'costDtl2',
                  headerName: 'Cost Forecast',
                  children: [{ field: 'costForecast' }],
                },
              ],
            },
          ],
          rows: [
            {
              id: 1,
              sno: 1,
              byProductName: 'Methanol',
              unit: 'MT',
              priceRsMt: 21000,
              normBudget: 1.2,
              normActual: 1.1,
              normForecast: 1.3,
              costBudget: 25000,
              costActual: 24500,
              costForecast: 25500,
            },
            {
              id: 2,
              sno: 2,
              byProductName: 'Ethanol',
              unit: 'MT',
              priceRsMt: 18000,
              normBudget: 0.8,
              normActual: 0.9,
              normForecast: 0.85,
              costBudget: 22000,
              costActual: 21500,
              costForecast: 22500,
            },
            {
              id: 3,
              sno: 3,
              byProductName: 'Isopropanol',
              unit: 'MT',
              priceRsMt: 24000,
              normBudget: 1.0,
              normActual: 0.95,
              normForecast: 1.05,
              costBudget: 26000,
              costActual: 25500,
              costForecast: 26500,
            },
            {
              id: 4,
              sno: 4,
              byProductName: 'Acetone',
              unit: 'MT',
              priceRsMt: 20000,
              normBudget: 1.5,
              normActual: 1.4,
              normForecast: 1.45,
              costBudget: 23000,
              costActual: 22500,
              costForecast: 23500,
            },
            {
              id: 5,
              sno: 5,
              byProductName: 'Phenol',
              unit: 'MT',
              priceRsMt: 27000,
              normBudget: 0.6,
              normActual: 0.65,
              normForecast: 0.7,
              costBudget: 28000,
              costActual: 27500,
              costForecast: 28500,
            },
          ],
        }

      case 'RawMaterial':
        // copy same shape as ByProducts but swap prefixes to 'raw'
        return {
          columns: [
            { field: 'sno', headerName: 'SL.No', align: 'right', flex: 0.5 },
            { field: 'rawName', headerName: 'Raw material', flex: 2 },
            { field: 'unit', headerName: 'Unit', flex: 1 },
            {
              field: 'rawPriceRsMt',
              headerName: 'Rs/MT',
              flex: 1,
              align: 'right',
            },
            {
              field: 'rawNormBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'rawNormActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'rawNormForecast',
              headerName: 'Forecast',
              flex: 1,
              align: 'right',
            },
            {
              field: 'rawCostBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'rawCostActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'rawCostForecast',
              headerName: 'Forecast',
              flex: 1,
              align: 'right',
            },
          ],
          columnGrouping: [
            {
              groupId: 'price',
              headerName: 'Price',
              children: [{ field: 'rawPriceRsMt' }],
            },
            {
              groupId: 'norm',
              headerName: 'Norm Unit/MT',
              children: [
                {
                  groupId: 'normDtl',
                  headerName: 'Norm Details',
                  children: [
                    { field: 'rawNormBudget' },
                    { field: 'rawNormActual' },
                  ],
                },
                {
                  groupId: 'normDtl2',
                  headerName: 'Norm Forecast',
                  children: [{ field: 'rawNormForecast' }],
                },
              ],
            },
            {
              groupId: 'cost',
              headerName: 'Cost Rs/MT',
              children: [
                {
                  groupId: 'costDtl',
                  headerName: 'Cost Details',
                  children: [
                    { field: 'rawCostBudget' },
                    { field: 'rawCostActual' },
                  ],
                },
                {
                  groupId: 'costDtl2',
                  headerName: 'Cost Forecast',
                  children: [{ field: 'rawCostForecast' }],
                },
              ],
            },
          ],
          rows: [
            {
              id: 1,
              sno: 1,
              rawName: 'Naphtha',
              unit: 'MT',
              rawPriceRsMt: 28000,
              rawNormBudget: 1.1,
              rawNormActual: 1.0,
              rawNormForecast: 1.2,
              rawCostBudget: 30800,
              rawCostActual: 30000,
              rawCostForecast: 31500,
            },
            {
              id: 2,
              sno: 2,
              rawName: 'Propylene',
              unit: 'MT',
              rawPriceRsMt: 34000,
              rawNormBudget: 0.9,
              rawNormActual: 0.85,
              rawNormForecast: 0.95,
              rawCostBudget: 30600,
              rawCostActual: 28900,
              rawCostForecast: 32300,
            },
            {
              id: 3,
              sno: 3,
              rawName: 'Ethylene',
              unit: 'MT',
              rawPriceRsMt: 31000,
              rawNormBudget: 1.2,
              rawNormActual: 1.15,
              rawNormForecast: 1.25,
              rawCostBudget: 37200,
              rawCostActual: 35650,
              rawCostForecast: 38750,
            },
          ],
        }

      case 'CatChem':
        return {
          columns: [
            { field: 'sno', headerName: 'SL.No', align: 'right', flex: 0.5 },
            { field: 'catalystName', headerName: 'Catalyst', flex: 2 },
            { field: 'unit', headerName: 'Unit', flex: 1 },
            {
              field: 'catPriceRsMt',
              headerName: 'Rs/MT',
              flex: 1,
              align: 'right',
            },
            {
              field: 'catNormBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'catNormActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'catNormForecast',
              headerName: 'Forecast',
              flex: 1,
              align: 'right',
            },
            {
              field: 'catCostBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'catCostActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'catCostForecast',
              headerName: 'Forecast',
              flex: 1,
              align: 'right',
            },
          ],
          columnGrouping: [
            {
              groupId: 'price',
              headerName: 'Price',
              children: [{ field: 'catPriceRsMt' }],
            },
            {
              groupId: 'norm',
              headerName: 'Norm Unit/MT',
              children: [
                {
                  groupId: 'normDtl',
                  headerName: 'Norm Details',
                  children: [
                    { field: 'catNormBudget' },
                    { field: 'catNormActual' },
                  ],
                },
                {
                  groupId: 'normDtl2',
                  headerName: 'Norm Forecast',
                  children: [{ field: 'catNormForecast' }],
                },
              ],
            },
            {
              groupId: 'cost',
              headerName: 'Cost Rs/MT',
              children: [
                {
                  groupId: 'costDtl',
                  headerName: 'Cost Details',
                  children: [
                    { field: 'catCostBudget' },
                    { field: 'catCostActual' },
                  ],
                },
                {
                  groupId: 'costDtl2',
                  headerName: 'Cost Forecast',
                  children: [{ field: 'catCostForecast' }],
                },
              ],
            },
          ],
          rows: [
            {
              id: 1,
              sno: 1,
              catalystName: 'Zeolite-Y',
              unit: 'Kg',
              catPriceRsMt: 50000,
              catNormBudget: 0.005,
              catNormActual: 0.0045,
              catNormForecast: 0.0052,
              catCostBudget: 250,
              catCostActual: 225,
              catCostForecast: 260,
            },
            {
              id: 2,
              sno: 2,
              catalystName: 'FCC Catalyst',
              unit: 'Kg',
              catPriceRsMt: 60000,
              catNormBudget: 0.006,
              catNormActual: 0.0058,
              catNormForecast: 0.0061,
              catCostBudget: 360,
              catCostActual: 348,
              catCostForecast: 366,
            },
          ],
        }

      case 'Utilities':
        return {
          columns: [
            { field: 'sno', headerName: 'SL.No', align: 'right', flex: 0.5 },
            { field: 'utilName', headerName: 'Utility', flex: 2 },
            { field: 'unit', headerName: 'Unit', flex: 1 },
            {
              field: 'utilPriceRsMt',
              headerName: 'Rs/MT',
              flex: 1,
              align: 'right',
            },
            {
              field: 'utilNormBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'utilNormActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'utilNormForecast',
              headerName: 'Forecast',
              flex: 1,
              align: 'right',
            },
            {
              field: 'utilCostBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'utilCostActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'utilCostForecast',
              headerName: 'Forecast',
              flex: 1,
              align: 'right',
            },
          ],
          columnGrouping: [
            {
              groupId: 'price',
              headerName: 'Price',
              children: [{ field: 'utilPriceRsMt' }],
            },
            {
              groupId: 'norm',
              headerName: 'Norm Unit/MT',
              children: [
                {
                  groupId: 'normDtl',
                  headerName: 'Norm Details',
                  children: [
                    { field: 'utilNormBudget' },
                    { field: 'utilNormActual' },
                  ],
                },
                {
                  groupId: 'normDtl2',
                  headerName: 'Norm Forecast',
                  children: [{ field: 'utilNormForecast' }],
                },
              ],
            },
            {
              groupId: 'cost',
              headerName: 'Cost Rs/MT',
              children: [
                {
                  groupId: 'costDtl',
                  headerName: 'Cost Details',
                  children: [
                    { field: 'utilCostBudget' },
                    { field: 'utilCostActual' },
                  ],
                },
                {
                  groupId: 'costDtl2',
                  headerName: 'Cost Forecast',
                  children: [{ field: 'utilCostForecast' }],
                },
              ],
            },
          ],
          rows: [
            {
              id: 1,
              sno: 1,
              utilName: 'Steam',
              unit: 'MT',
              utilPriceRsMt: 3500,
              utilNormBudget: 0.4,
              utilNormActual: 0.38,
              utilNormForecast: 0.42,
              utilCostBudget: 1400,
              utilCostActual: 1330,
              utilCostForecast: 1470,
            },
            {
              id: 2,
              sno: 2,
              utilName: 'Electricity',
              unit: 'kWh',
              utilPriceRsMt: 7,
              utilNormBudget: 25,
              utilNormActual: 24,
              utilNormForecast: 26,
              utilCostBudget: 175,
              utilCostActual: 168,
              utilCostForecast: 182,
            },
          ],
        }

      case 'OtherVars':
        return {
          columns: [
            { field: 'sno', headerName: 'SL.No', align: 'right' },
            { field: 'description', headerName: 'Description', flex: 2 },
            { field: 'unit', headerName: 'Unit' },
            {
              field: 'ovcBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'ovcActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'ovcBudget2',
              headerName: 'Budget2',
              flex: 1,
              align: 'right',
            },
          ],
          columnGrouping: [
            {
              groupId: 'cost',
              headerName: 'Cost Rs/MT',
              children: [{ field: 'ovcBudget' }, { field: 'ovcActual' }],
            },
            {
              groupId: 'cost2',
              headerName: 'Cost2 Rs/MT',
              children: [{ field: 'ovcBudget2' }],
            },
          ],
          rows: [
            {
              id: 1,
              sno: 1,
              description: 'Lab charges',
              unit: 'MT',
              ovcBudget: 15,
              ovcActual: 14,
              ovcBudget2: 15,
            },
            {
              id: 2,
              sno: 2,
              description: 'Maintenance spares',
              unit: 'MT',
              ovcBudget: 25,
              ovcActual: 27,
              ovcBudget2: 25,
            },
            {
              id: 3,
              sno: 3,
              description: 'One-time setup',
              unit: 'MT',
              ovcBudget: 100,
              ovcActual: 90,
              ovcBudget2: 100,
            },
          ],
        }

      case 'ProdCalc':
        return {
          columns: [
            { field: 'sno', headerName: 'SL.No', align: 'right' },
            { field: 'description', headerName: 'Metric', flex: 2 },
            {
              field: 'rateBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
            {
              field: 'rateActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
            },
            {
              field: 'valueBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
            },
          ],
          columnGrouping: [
            {
              groupId: 'rate',
              headerName: 'Rate Rs/MT',
              children: [{ field: 'rateBudget' }, { field: 'rateActual' }],
            },
            {
              groupId: 'value',
              headerName: 'Value Rs Cr',
              children: [{ field: 'valueBudget' }],
            },
          ],
          rows: [
            {
              id: 1,
              sno: 1,
              description: 'Total conversion cost',
              rateBudget: 250,
              rateActual: 255,
              valueBudget: 500,
            },
            {
              id: 2,
              sno: 2,
              description: 'Total variable cost',
              rateBudget: 400,
              rateActual: 390,
              valueBudget: 800,
            },
            {
              id: 3,
              sno: 3,
              description: 'Total contribution',
              rateBudget: 650,
              rateActual: 645,
              valueBudget: 1300,
            },
          ],
        }

      default:
        throw new Error(`Unknown category: ${category}`)
    }
  },
}
