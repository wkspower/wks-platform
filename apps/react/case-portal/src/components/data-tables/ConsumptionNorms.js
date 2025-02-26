import { DataService } from 'services/DataService'
import DataGridTable from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'

const NormalOpNormsScreen = () => {
  const keycloak = useSession();
  const [csData, setCsData] = useState([]);
  const [csDataTransformed, setCsDataTransformed] = useState([]);
  const [allProducts, setAllProducts] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await DataService.getConsumptionNormsData(keycloak);
        setCsData(data);

        let rowIndex = 1;
        const groupedRows = [];

        Object.entries(data).forEach(([category, rows], index) => {
          // Group header with a unique negative ID
          groupedRows.push({
            id: `group-${index}`,
            category: category,
          });

          // Add data rows with unique IDs
          rows.forEach((row) => {
            groupedRows.push({
              ...row,
              id: row.NormParameterMonthlyTransactionId || `row-${rowIndex++}`,
            });
          });
        });

        setCsDataTransformed(groupedRows);
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak);
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }));
        setAllProducts(productList);
      } catch (error) {
        console.error("Error fetching products:", error);
      }
    };

    getAllProducts();
    fetchData();
  }, []);

  const productionColumns = [
    { field: "category", headerName: "Particulars", width: 150, editable: true },
    { field: "TPH", headerName: "Unit", width: 100, editable: true },
    // { field: "norms", headerName: "Norms", width: 100, editable: true },
    { field: "apr24", headerName: "Apr-24", width: 100, editable: true },
    { field: "may24", headerName: "May-24", width: 100, editable: true },
    { field: "jun24", headerName: "Jun-24", width: 100, editable: true },
    { field: "jul24", headerName: "Jul-24", width: 100, editable: true },
    { field: "aug24", headerName: "Aug-24", width: 100, editable: true },
    { field: "sep24", headerName: "Sep-24", width: 100, editable: true },
    { field: "oct24", headerName: "Oct-24", width: 100, editable: true },
    { field: "nov24", headerName: "Nov-24", width: 100, editable: true },
    { field: "dec24", headerName: "Dec-24", width: 100, editable: true },
    { field: "jan25", headerName: "Jan-25", width: 100, editable: true },
    { field: "feb25", headerName: "Feb-25", width: 100, editable: true },
    { field: "mar25", headerName: "Mar-25", width: 100, editable: true },
    { field: "remark", headerName: "Remark", width: 180, editable: true },
  ];


  return (
    <div>
      <DataGridTable
        columns={productionColumns}
        rows={csDataTransformed}
        getRowId={(row) => row.id}
        title="Consumption Norms"
        paginationOptions={[100, 200, 300]}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
        }}
      />
    </div>
  );
};


export default NormalOpNormsScreen
