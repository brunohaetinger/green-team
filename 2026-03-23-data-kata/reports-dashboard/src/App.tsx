import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "./components/ui/table"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "./components/ui/card"

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
  const [salesData, setSalesData] = useState([]);

  useEffect(() => {
    const getSalesByCity = async () => {
      const res = await fetch("http://report-api:8080/api/v1/sales/rankings/top-sales-by-city")
      const resJson = res.json();
      setSalesData(resJson);
    }
    getSalesByCity();
  }, [])

  return (
    <div className="min-h-screen bg-background py-12 px-4">
      <div className="max-w-4xl mx-auto space-y-8">
        <header className="space-y-2">
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            Top Sales Rankings
          </h1>
          <p className="text-muted-foreground">
            Overview of the best performing cities by revenue
          </p>
        </header>

        <Card className="shadow-sm border-border/60 bg-card">
          <CardHeader className="pb-4">
            <CardTitle className="text-lg text-foreground">Sales by City</CardTitle>
            <CardDescription>
              Ranked by total revenue amount
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="rounded-lg border border-border overflow-hidden bg-card">
              <Table>
                <TableHeader>
                  <TableRow className="bg-muted/40 hover:bg-muted/40 border-border">
                    <TableHead className="w-16 font-medium text-muted-foreground">
                      Rank
                    </TableHead>
                    <TableHead className="font-medium text-muted-foreground">
                      City
                    </TableHead>
                    <TableHead className="font-medium text-muted-foreground">
                      Country
                    </TableHead>
                    <TableHead className="text-right font-medium text-muted-foreground">
                      Amount
                    </TableHead>
                    <TableHead className="text-right font-medium text-muted-foreground">
                      Units
                    </TableHead>
                    <TableHead className="text-right font-medium text-muted-foreground">
                      Orders
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {salesData.map((data, index) => (
                    <TableRow
                      key={index}
                      className="hover:bg-muted/20 transition-colors border-border"
                    >
                      <TableCell className="font-medium text-primary">
                        #{index + 1}
                      </TableCell>
                      <TableCell className="font-medium text-foreground">{data.city}</TableCell>
                      <TableCell className="text-muted-foreground">
                        {data.country}
                      </TableCell>
                      <TableCell className="text-right font-mono tabular-nums text-emerald-400">
                        ${data.amount.toLocaleString()}
                      </TableCell>
                      <TableCell className="text-right font-mono tabular-nums text-muted-foreground">
                        {data.units.toLocaleString()}
                      </TableCell>
                      <TableCell className="text-right font-mono tabular-nums text-muted-foreground">
                        {data.orders.toLocaleString()}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

export default App
function useState(arg0: never[]): [any, any] {
  throw new Error("Function not implemented.")
}
function useEffect(arg0: () => void, arg1: never[]) {
  throw new Error("Function not implemented.")
}
