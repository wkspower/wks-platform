export const MockPlantContributionAPILastFourYears = {
  async getReport({
    category,
    AOP_YEAR,
    verticalName,
    FORMAT_VALUES_3_DECIMAL,
    FORMAT_VALUES_2_DECIMAL,
    FORMAT_VALUES_COST,
    FORMAT_VALUES_PRICE,
  }) {
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
      // ==== A. Product mix and production ====
      case 'ProductMixAndProduction':
        return {
          columns: [
            {
              field: 'rowNo',
              title: 'S.no',
              widthT: 58,
              editable: false,
              format: '{0:#.#}',
              align: 'right',
            },
            {
              field: 'material',
              title: 'Product name',
              editable: false,
            },
            {
              field: 'uom',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,
                  align: 'right',

                  format: FORMAT_VALUES_PRICE,
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
                      field: 'actualFourYearsAgo',
                      title: 'Actual',
                      editable: false,
                      align: 'right',

                      format: FORMAT_VALUES_2_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'actualThreeYearsAgo',
                      title: 'Actual',
                      editable: false,
                      align: 'right',

                      format: FORMAT_VALUES_2_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'actualTwoYearsAgo',
                      title: 'Actual',
                      editable: false,
                      align: 'right',

                      format: FORMAT_VALUES_2_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'actualLastYear',
                      title: 'Actual',
                      editable: false,
                      align: 'right',

                      format: FORMAT_VALUES_2_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'budgetCurrent',
                      title: 'Budget',
                      editable: false,
                      align: 'right',

                      format: FORMAT_VALUES_2_DECIMAL,
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
              field: 'rowNo',
              title: 'S.no',
              widthT: 58,
              format: '{0:#.#}',
              editable: false,
              align: 'right',
            },
            {
              field: 'material',
              title: 'Product Name',
              editable: false,
            },
            {
              field: 'uom',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,

                  align: 'right',
                  format: FORMAT_VALUES_PRICE,
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
                      field: 'actualFourYearsAgo',
                      title: 'Actual',
                      editable: false,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'actualThreeYearsAgo',
                      title: 'Actual',
                      editable: false,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'actualTwoYearsAgo',
                      title: 'Actual',
                      editable: false,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'actualLastYear',
                      title: 'Actual',
                      editable: false,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'budgetCurrent',
                      title: 'Budget',
                      editable: false,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
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
                      field: 'actualFourYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'actualThreeYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'actualTwoYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'actualLastYearCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'budgetCurrentCost',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
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
              field: 'rowNo',
              title: 'S.no',
              widthT: 58,
              format: '{0:#.#}',

              editable: false,
              align: 'right',
            },
            {
              field: 'material',
              title: 'Product Name',
              editable: false,
            },
            {
              field: 'uom',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,

                  align: 'right',
                  format: FORMAT_VALUES_PRICE,
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
                      field: 'actualFourYearsAgo',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'actualThreeYearsAgo',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'actualTwoYearsAgo',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'actualLastYear',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'budgetCurrent',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
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
                      field: 'actualFourYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'actualThreeYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'actualTwoYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'actualLastYearCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'budgetCurrentCost',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
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
              field: 'rowNo',
              title: 'S.no',
              widthT: 58,
              format: '{0:#.#}',

              editable: false,
              align: 'right',
            },
            {
              field: 'material',
              title: 'Product Name',
              editable: false,
            },
            {
              field: 'uom',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,

                  align: 'right',
                  format: FORMAT_VALUES_PRICE,
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
                      field: 'actualFourYearsAgo',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'actualThreeYearsAgo',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'actualTwoYearsAgo',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'actualLastYear',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'budgetCurrent',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
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
                      field: 'actualFourYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'actualThreeYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'actualTwoYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'actualLastYearCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'budgetCurrentCost',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
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
              field: 'rowNo',
              title: 'S.no',
              widthT: 58,
              format: '{0:#.#}',

              editable: false,
              align: 'right',
            },
            {
              field: 'material',
              title: 'Product Name',
              editable: false,
            },
            {
              field: 'uom',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,

                  align: 'right',
                  format: FORMAT_VALUES_PRICE,
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
                      field: 'actualFourYearsAgo',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'actualThreeYearsAgo',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'actualTwoYearsAgo',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'actualLastYear',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'budgetCurrent',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
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
                      field: 'actualFourYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY3,
                  children: [
                    {
                      field: 'actualThreeYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY2,
                  children: [
                    {
                      field: 'actualTwoYearsAgoCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: prevFY1,
                  children: [
                    {
                      field: 'actualLastYearCost',
                      title: 'Actual',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'budgetCurrentCost',
                      title: 'Budget',
                      editable: false,

                      align: 'right',
                      format: FORMAT_VALUES_COST,
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
              field: 'rowNo',
              title: 'S.no',
              widthT: 58,
              align: 'right',
              editable: false,
              format: '{0:#.#}',
            },
            {
              field: 'id',
              hidden: true,
            },
            {
              field: 'material',
              title: 'Other cost',
              editable: false,
            },
            {
              field: 'uom',
              widthT: 60,
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
            },
            {
              title: prevFY4,
              children: [
                {
                  field: 'actualFourYearsAgo',
                  title: 'Actual',
                  align: 'right',
                  editable: true,
                  format: FORMAT_VALUES_COST,
                  type: 'numberNonGrey',
                },
              ],
            },
            {
              title: prevFY3,
              children: [
                {
                  field: 'actualThreeYearsAgo',
                  title: 'Actual',
                  align: 'right',
                  editable: true,
                  format: FORMAT_VALUES_COST,
                  type: 'numberNonGrey',
                },
              ],
            },
            {
              title: prevFY2,
              children: [
                {
                  field: 'actualTwoYearsAgo',
                  title: 'Actual',
                  align: 'right',
                  editable: true,
                  format: FORMAT_VALUES_COST,
                  type: 'numberNonGrey',
                },
              ],
            },
            {
              title: prevFY1,
              children: [
                {
                  field: 'actualLastYear',
                  title: 'Actual',
                  align: 'right',
                  editable: true,
                  format: FORMAT_VALUES_COST,
                  type: 'numberNonGrey',
                },
              ],
            },
            {
              title: currFY, // e.g., '2024-25'
              children: [
                {
                  field: 'budgetCurrent',
                  title: 'Budget',
                  align: 'right',
                  editable: true,
                  format: FORMAT_VALUES_COST,
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
              field: 'rowNo',
              title: 'S.no',
              widthT: 58,
              format: '{0:#.#}',
              align: 'right',
              editable: false,
            },
            {
              field: 'material',
              title: 'Production cost calculations',
              width: 220,
              editable: false,
            },
            {
              title: prevFY4, // e.g., '2023-24'
              children: [
                {
                  field: 'actualFourYearsAgo',
                  title: 'Actual',
                  align: 'right',
                  editable: false,
                  format: FORMAT_VALUES_COST,
                  type: 'number',
                },
              ],
            },
            {
              title: prevFY3, // e.g., '2023-24'
              children: [
                {
                  field: 'actualThreeYearsAgo',
                  title: 'Actual',
                  align: 'right',
                  editable: false,
                  format: FORMAT_VALUES_COST,
                  type: 'number',
                },
              ],
            },
            {
              title: prevFY2, // e.g., '2023-24'
              children: [
                {
                  field: 'actualTwoYearsAgo',
                  title: 'Actual',
                  align: 'right',
                  editable: false,
                  format: FORMAT_VALUES_COST,
                  type: 'number',
                },
              ],
            },
            {
              title: prevFY1, // e.g., '2023-24'
              children: [
                {
                  field: 'ActualLastYear',
                  title: 'Actual',

                  align: 'right',
                  editable: false,
                  format: FORMAT_VALUES_COST,
                  type: 'number',
                },
              ],
            },
            {
              title: currFY,
              children: [
                {
                  field: 'BudgetCurrent',
                  title: 'Budget',

                  align: 'right',
                  editable: false,
                  format: FORMAT_VALUES_COST,
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
