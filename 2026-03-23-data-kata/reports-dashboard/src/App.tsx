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
  return (
    <div className="min-h-screen bg-slate-50/50 dark:bg-slate-950/50 py-12 px-4">
      <div className="max-w-4xl mx-auto space-y-8">
        <header className="space-y-2">
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            Top Sales Rankings
          </h1>
          <p className="text-muted-foreground">
            Overview of the best performing cities by revenue
          </p>
        </header>

        <Card className="shadow-sm border-border/60">
          <CardHeader className="pb-4">
            <CardTitle className="text-lg">Sales by City</CardTitle>
            <CardDescription>
              Ranked by total revenue amount
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="rounded-lg border overflow-hidden">
              <Table>
                <TableHeader>
                  <TableRow className="bg-muted/50 hover:bg-muted/50">
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
                  {mockSalesData.map((data, index) => (
                    <TableRow
                      key={index}
                      className="hover:bg-muted/30 transition-colors"
                    >
                      <TableCell className="font-medium text-muted-foreground">
                        {index + 1}
                      </TableCell>
                      <TableCell className="font-medium">{data.city}</TableCell>
                      <TableCell className="text-muted-foreground">
                        {data.country}
                      </TableCell>
                      <TableCell className="text-right font-mono tabular-nums">
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
