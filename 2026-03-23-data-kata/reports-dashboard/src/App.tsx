import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "./components/ui/table"

interface SalesData {
  city: string
  country: string
  amount: number
  units: number
  orders: number
}

const mockSalesData: SalesData[] = [
  { city: "New York", country: "USA", amount: 125000, units: 450, orders: 120 },
  { city: "London", country: "UK", amount: 98500, units: 320, orders: 95 },
  { city: "Tokyo", country: "Japan", amount: 87200, units: 280, orders: 78 },
]

function App() {
  return (
    <div className="p-8">
      <h1 className="mb-8">Top Sales Rankings</h1>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[50px]">#</TableHead>
            <TableHead>City</TableHead>
            <TableHead>Country</TableHead>
            <TableHead className="text-right">Amount</TableHead>
            <TableHead className="text-right">Units</TableHead>
            <TableHead className="text-right">Orders</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {mockSalesData.map((data, index) => (
            <TableRow key={index}>
              <TableCell className="font-medium">{index + 1}</TableCell>
              <TableCell>{data.city}</TableCell>
              <TableCell>{data.country}</TableCell>
              <TableCell className="text-right">${data.amount.toLocaleString()}</TableCell>
              <TableCell className="text-right">{data.units.toLocaleString()}</TableCell>
              <TableCell className="text-right">{data.orders.toLocaleString()}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

export default App
