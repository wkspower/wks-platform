export const MockSpecificConsumptionNormsAPI = {
  async getReport({ category, AOP_YEAR, valueFormat }) {

    const currFY = AOP_YEAR || ''
    let prevFY1 = ''
    let prevFY2 = ''
    let prevFY3 = ''
    let prevFY4 = ''
    if (currFY.includes('-')) {
      const [start, end] = currFY.split('-').map(Number)
      prevFY1 = `${start - 1}-${(end - 1).toString().padStart(2, '0')}`
      prevFY2 = `${start - 2}-${(end - 2).toString().padStart(2, '0')}`
      prevFY3 = `${start - 3}-${(end - 3).toString().padStart(2, '0')}`
      prevFY4 = `${start - 4}-${(end - 4).toString().padStart(2, '0')}`
    }

    switch (category) {
      case 'RawMaterial':
        return {
          columns: [
            { field: 'sno', title: 'S.no', widthT: 58, editable: false, align: 'right', format: '{0:0}' },
            { field: 'materialID', title: 'Material ID', editable: false },
            { field: 'utilityName', title: 'Raw material name', editable: false },
            { field: 'unit', title: 'Unit', widthT: 60, editable: false },
            { field: 'design', title: 'Design', editable: false },
            { field: 'bestAchievedSinceLastFourYears', title: 'Best achieved\nsince last 4 years', editable: false },
            { field: 'globalBenchmark', title: 'Global benchmark\n(Give reference)', editable: false },
            {
              title: prevFY4,
              children: [
                { field: 'actualFourYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY3,
              children: [
                { field: 'actualThreeYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY2,
              children: [
                { field: 'actualTwoYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY1,
              children: [
                { field: 'budgetLastYear', title: 'Budget', editable: false, align: 'right', format: valueFormat, type: 'number' },
                { field: 'actualLastYear', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' },
              ],
            },
            {
              title: currFY,
              children: [
                { field: 'budgetProposed', title: 'Proposed Norm', editable: false }
              ],
            },
          ],
        }

      case 'ByProduct':
        return {
          columns: [
            { field: 'sno', title: 'S.no', widthT: 58, editable: false, align: 'right', format: '{0:0}' },
            { field: 'materialID', title: 'Material ID', editable: false },
            { field: 'utilityName', title: 'By Product Name', editable: false },
            { field: 'unit', title: 'Unit', widthT: 60, editable: false },
            { field: 'design', title: 'Design', editable: false },
            { field: 'bestAchievedSinceLastFourYears', title: 'Best achieved\nsince last 4 years', editable: false },
            { field: 'globalBenchmark', title: 'Global benchmark\n(Give reference)', editable: false },
            {
              title: prevFY4,
              children: [
                { field: 'actualFourYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY3,
              children: [
                { field: 'actualThreeYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY2,
              children: [
                { field: 'actualTwoYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY1,
              children: [
                { field: 'budgetLastYear', title: 'Budget', editable: false, align: 'right', format: valueFormat, type: 'number' },
                { field: 'actualLastYear', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' },
              ],
            },
            {
              title: currFY,
              children: [
                { field: 'budgetProposed', title: 'Proposed Norm', editable: false }
              ],
            },
          ],
        }

      case 'CatChem':
        return {
          columns: [
            { field: 'sno', title: 'S.no', widthT: 58, editable: false, align: 'right', format: '{0:0}' },
            { field: 'materialID', title: 'Material ID', editable: false },
            { field: 'utilityName', title: 'Catchem Name', editable: false },
            { field: 'unit', title: 'Unit', widthT: 60, editable: false },
            { field: 'design', title: 'Design', editable: false },
            { field: 'bestAbestAchievedSinceLastFourYearschieved', title: 'Best achieved\nsince last 4 years', editable: false },
            { field: 'globalBenchmark', title: 'Global benchmark\n(Give reference)', editable: false },
            {
              title: prevFY4,
              children: [
                { field: 'actualFourYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY3,
              children: [
                { field: 'actualThreeYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY2,
              children: [
                { field: 'actualTwoYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY1,
              children: [
                { field: 'budgetLastYear', title: 'Budget', editable: false, align: 'right', format: valueFormat, type: 'number' },
                { field: 'actualLastYear', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' },
              ],
            },
            {
              title: currFY,
              children: [
                { field: 'budgetProposed', title: 'Proposed Norm', editable: false }
              ],
            },
          ],
        }

      case 'Utilities':
        return {
          columns: [
            { field: 'sno', title: 'S.no', widthT: 58, editable: false, align: 'right', format: '{0:0}' },
            { field: 'materialID', title: 'Material ID', editable: false },
            { field: 'utilityName', title: 'Utilities Name', editable: false },
            { field: 'unit', title: 'Unit', widthT: 60, editable: false },
            { field: 'design', title: 'Design', editable: false },
            { field: 'bestAchievedSinceLastFourYears', title: 'Best achieved\nsince last 4 years', editable: false },
            { field: 'globalBenchmark', title: 'Global benchmark\n(Give reference)', editable: false },
            {
              title: prevFY4,
              children: [
                { field: 'actualFourYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY3,
              children: [
                { field: 'actualThreeYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY2,
              children: [
                { field: 'actualTwoYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY1,
              children: [
                { field: 'budgetLastYear', title: 'Budget', editable: false, align: 'right', format: valueFormat, type: 'number' },
                { field: 'actualLastYear', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' },
              ],
            },
            {
              title: currFY,
              children: [
                { field: 'budgetProposed', title: 'Proposed Norm', editable: false }
              ],
            },
          ],
        }

      case 'QualityParameters':
        return {
          columns: [
            { field: 'sno', title: 'S.no', widthT: 58, editable: false, align: 'right', format: '{0:0}' },
            { field: 'materialID', title: 'Material ID', editable: false },
            { field: 'utilityName', title: 'name', editable: false },
            { field: 'unit', title: 'Unit', widthT: 60, editable: false },
            { field: 'design', title: 'Design', editable: false },
            { field: 'bestAchievedSinceLastFourYears', title: 'Best achieved\nsince last 4 years', editable: false },
            { field: 'globalBenchmark', title: 'Global benchmark\n(Give reference)', editable: false },
            {
              title: prevFY4,
              children: [
                { field: 'actualFourYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY3,
              children: [
                { field: 'actualThreeYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY2,
              children: [
                { field: 'actualTwoYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY1,
              children: [
                { field: 'budgetCurrent', title: 'Budget', editable: false, align: 'right', format: '{0:#.###}', type: 'number' },
                { field: 'actualCurrent', title: 'Actual', editable: false, align: 'right', format: '{0:#.###}', type: 'number' },
              ],
            },
            {
              title: currFY,
              children: [
                { field: 'proposedNorm', title: 'Proposed Norm', editable: false }
              ],
            },
          ],
        }
    case 'OtherVariable':
        return {
          columns: [
            { field: 'sno', title: 'S.no', widthT: 58, editable: false, align: 'right', format: '{0:0}' },
            { field: 'materialID', title: 'Material ID', editable: false },
            { field: 'utilityName', title: 'Other Cost', editable: false },
            { field: 'unit', title: 'Unit', widthT: 60, editable: false },
            { field: 'design', title: 'Design', editable: false },
            { field: 'bestAchievedSinceLastFourYears', title: 'Best achieved\nsince last 4 years', editable: false },
            { field: 'globalBenchmark', title: 'Global benchmark\n(Give reference)', editable: false },
            {
              title: prevFY4,
              children: [
                { field: 'actualFourYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY3,
              children: [
                { field: 'actualThreeYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY2,
              children: [
                { field: 'actualTwoYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY1,
              children: [
                { field: 'budgetLastYear', title: 'Budget', editable: false, align: 'right', format: valueFormat, type: 'number' },
                { field: 'actualLastYear', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' },
              ],
            },
            {
              title: currFY,
              children: [
                { field: 'budgetProposed', title: 'Proposed Norm', editable: false }
              ],
            },
          ],
        }


      case 'PackingConsumables':
        return {
          columns: [
            { field: 'rowNo', title: 'S.no', widthT: 58, editable: false, align: 'right', format: '{0:0}' },
            { field: 'material', title: 'Material ID', editable: false },
            { field: 'material', title: 'name of item', editable: false },
            { field: 'uom', title: 'Unit', widthT: 60, editable: false },
            { field: 'design', title: 'Design', editable: false },
            { field: 'bestAchieved', title: 'Best achieved\nsince last 4 years', editable: false },
            { field: 'globalBenchmark', title: 'Global benchmark\n(Give reference)', editable: false },
            {
              title: prevFY4,
              children: [
                { field: 'actualFourYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY3,
              children: [
                { field: 'actualThreeYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY2,
              children: [
                { field: 'actualTwoYearsAgo', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' }
              ],
            },
            {
              title: prevFY1,
              children: [
                { field: 'budgetCurrent', title: 'Budget', editable: false, align: 'right', format: valueFormat, type: 'number' },
                { field: 'actualCurrent', title: 'Actual', editable: false, align: 'right', format: valueFormat, type: 'number' },
              ],
            },
            {
              title: currFY,
              children: [
                { field: 'budgetProposed', title: 'Proposed Norm', editable: false }
              ],
            },
            { field: 'ratePerUnit', title: 'Rate/Unit', editable: false, align: 'right', format: valueFormat, type: 'number' },
      {
        title: prevFY1,
        children: [
          { field: 'budgetRsPerMT', title: 'Budget Rs./MT', editable: false, align: 'right', format: valueFormat, type: 'number' },
          { field: 'actualRsPerMT', title: 'Actual Rs./MT', editable: false, align: 'right', format: valueFormat, type: 'number' },
        ],
      },
      {
        title: currFY, // e.g., 2025-26
        children: [
          { field: 'proposedRsPerMT', title: 'Proposed Rs./MT', editable: false, align: 'right', format: valueFormat, type: 'number' },
        ],
      },
          ],
        }

      default:
        return { columns: [] }
    }
  },
}  