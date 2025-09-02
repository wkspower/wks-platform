export const MockPlantContributionAPILastFourYears = {
  async getReport({ category, year, verticalName }) {
    const currFY = year || ''
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
      // ==== A. Product mix and production ====
      case 'ProductMixAndProduction':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'S.no',
              widthT: 58,
              editable: false,

              align: 'right',
            },
            {
              field: 'ByProductName',
              title: 'Product name',
              editable: false,
            },
            {
              field: 'Unit',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,
                  align: 'right',

                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
            {
              title: 'Production, MT',
              children: [
                {
                  title: prevFY4,
                  children: [
                    {
                      field: 'PrevYearFourNormActual',
                      title: 'Actual',
                      editable: false,
                      align: 'right',

                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'PrevYearThreeNormActual',
                      title: 'Actual',
                      editable: false,
                      align: 'right',

                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'PrevYearTwoNormActual',
                      title: 'Actual',
                      editable: false,
                      align: 'right',

                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'PrevYearOneNormActual',
                      title: 'Actual',
                      editable: false,
                      align: 'right',

                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearCostBudget',
                      title: 'Budget',
                      editable: false,
                      align: 'right',

                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== B. By products ====
      case 'ByProducts':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'S.no',
              widthT: 58,

              editable: false,
              align: 'right',
            },
            {
              field: 'ByProductName',
              title: 'By product name',
              editable: false,
            },
            {
              field: 'Unit',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,

                  align: 'right',
                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
            {
              title: 'Specific Consumptions',
              children: [
                {
                  title: prevFY4,
                  children: [
                    {
                      field: 'PrevYear1NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'PrevYear2NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'PrevYear3NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'PrevYear4NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'CurrYearNormBudget',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
              ],
            },
            {
              title: 'Cost Rs/MT',
              children: [
                {
                  title: prevFY4,
                  children: [
                    {
                      field: 'PrevYear1CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'PrevYear2CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'PrevYear3CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'PrevYear4CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'CurrYearCostBudget',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== C. Raw material ====
      case 'RawMaterial':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'S.no',
              widthT: 58,

              editable: false,
              align: 'right',
            },
            {
              field: 'ByProductName',
              title: 'Raw material name',
              editable: false,
            },
            {
              field: 'Unit',
              widthT: 60,
              title: 'UOM',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,

                  align: 'right',
                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
            {
              title: 'Specific Consumptions',
              children: [
                {
                  title: prevFY4,
                  children: [
                    {
                      field: 'PrevYear1NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'PrevYear2NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'PrevYear3NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'PrevYear4NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'CurrYearNormBudget',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
              ],
            },
            {
              title: 'Cost Rs/MT',
              children: [
                {
                  title: prevFY4,
                  children: [
                    {
                      field: 'PrevYear1CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'PrevYear2CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'PrevYear3CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'PrevYear4CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'CurrYearCostBudget',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== D. Cat chem ====
      case 'CatChem':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'S.no',
              widthT: 58,

              editable: false,
              align: 'right',
            },
            {
              field: 'ByProductName',
              title: 'Catalyst name',
              editable: false,
            },
            {
              field: 'Unit',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,

                  align: 'right',
                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
            {
              title: 'Specific Consumptions',
              children: [
                {
                  title: prevFY4,
                  children: [
                    {
                      field: 'PrevYear1NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'PrevYear2NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'PrevYear3NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'PrevYear4NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'CurrYearNormBudget',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
              ],
            },
            {
              title: 'Cost Rs/MT',
              children: [
                {
                  title: prevFY4,
                  children: [
                    {
                      field: 'PrevYear1CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'PrevYear2CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'PrevYear3CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'PrevYear4CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'CurrYearCostBudget',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== E. Utilities ====
      case 'Utilities':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'S.no',
              widthT: 58,

              editable: false,
              align: 'right',
            },
            {
              field: 'ByProductName',
              title: 'Utility name',
              editable: false,
            },
            {
              field: 'Unit',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,

                  align: 'right',
                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
            {
              title: 'Specific Consumptions',
              children: [
                {
                  title: prevFY4,
                  children: [
                    {
                      field: 'PrevYear1NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'PrevYear2NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'PrevYear3NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'PrevYear4NormActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'CurrYearNormBudget',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
              ],
            },
            {
              title: 'Cost Rs/MT',
              children: [
                {
                  title: prevFY4,
                  children: [
                    {
                      field: 'PrevYear1CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'PrevYear2CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'PrevYear3CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'PrevYear4CostActual',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'CurrYearCostBudget',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: '{0:#.##}',
                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== F. Other Variable Cost ====
      case 'OtherVariableCost':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'S.no',
              widthT: 58,
              align: 'right',
              editable: false,
            },
            {
              field: 'id',
              hidden: true,
            },
            {
              field: 'OtherCost',
              title: 'Other cost',
              editable: false,
            },
            {
              field: 'Unit',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: prevFY4,
              children: [
                {
                  field: 'PrevYearActual',
                  title: 'Actual',
                  align: 'right',
                  editable: true,
                  format: '{0:#.##}',
                  type: 'numberNonGrey',
                },
              ],
            },
            {
              title: prevFY3,
              children: [
                {
                  field: 'PrevYearActual',
                  title: 'Actual',
                  align: 'right',
                  editable: true,
                  format: '{0:#.##}',
                  type: 'numberNonGrey',
                },
              ],
            },
            {
              title: prevFY2,
              children: [
                {
                  field: 'PrevYearActual',
                  title: 'Actual',
                  align: 'right',
                  editable: true,
                  format: '{0:#.##}',
                  type: 'numberNonGrey',
                },
              ],
            },
            {
              title: prevFY1,
              children: [
                {
                  field: 'PrevYearActual',
                  title: 'Actual',
                  align: 'right',
                  editable: true,
                  format: '{0:#.##}',
                  type: 'numberNonGrey',
                },
              ],
            },
            {
              title: currFY, // e.g., '2024-25'
              children: [
                {
                  field: 'CurrentYearBudget',
                  title: 'Budget',
                  align: 'right',
                  editable: true,
                  format: '{0:#.##}',
                  type: 'numberNonGrey',
                },
              ],
            },
          ],
        }

      // ==== G. Production Cost Calculations ====
      case 'ProductionCostCalculations':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'S.no',
              widthT: 58,
              align: 'right',
              editable: false,
            },
            {
              field: 'ProductionCostCalculations',
              title: 'Production cost calculations',
              width: 220,
              editable: false,
            },
            {
              title: prevFY4, // e.g., '2023-24'
              children: [
                {
                  field: 'PrevYearActual',
                  title: 'Actual',
                  align: 'right',
                  editable: false,
                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
            {
              title: prevFY3, // e.g., '2023-24'
              children: [
                {
                  field: 'PrevYearActual',
                  title: 'Actual',
                  align: 'right',
                  editable: false,
                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
            {
              title: prevFY2, // e.g., '2023-24'
              children: [
                {
                  field: 'PrevYearActual',
                  title: 'Actual',
                  align: 'right',
                  editable: false,
                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
            {
              title: prevFY1, // e.g., '2023-24'
              children: [
                {
                  field: 'PrevYearActual',
                  title: 'Actual',

                  align: 'right',
                  editable: false,
                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
            {
              title: currFY,
              children: [
                {
                  field: 'NextYearBudget',
                  title: 'Budget',

                  align: 'right',
                  editable: false,
                  format: '{0:#.##}',
                  type: 'number',
                },
              ],
            },
          ],
        }

      default:
        throw new Error(`Unknown category: ${category}`)
    }
  },
}
