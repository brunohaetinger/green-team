import { useState, useEffect } from "react"
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
  cityName: string
  countryName: string
  totalAmount: number
  totalUnits: number
}

interface SalesmanData {
  salesmanName: string
  cityName: string
  countryName: string
  totalAmount: number
  totalUnits: number
}

function App() {
  const [salesData, setSalesData] = useState<SalesData[]>([]);
  const [salesmanData, setSalesmanData] = useState<SalesmanData[]>([]);

  useEffect(() => {
    const getSalesByCity = async () => {
      const res = await fetch("http://localhost:8080/api/v1/sales/rankings/top-sales-by-city")
      const resJson = await res.json();
      setSalesData(resJson.content);
    }
    const getTopSalesman = async () => {
      const res = await fetch("http://localhost:8080/api/v1/sales/rankings/top-salesman")
      const resJson = await res.json();
      setSalesmanData(resJson.content);
    }
    getSalesByCity();
    getTopSalesman();
  }, [])

  return (
    <div className="min-h-screen bg-background py-12 px-4">
      <div className="max-w-6xl mx-auto space-y-8">
        <header className="space-y-2">
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            Top Sales Rankings
          </h1>
          <p className="text-muted-foreground">
            Overview of the best performing cities by revenue
          </p>
        </header>

        <div className="grid grid-cols-2 gap-6">
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
                    <TableHead className="text-left font-medium text-muted-foreground">
                      Amount
                    </TableHead>
                    <TableHead className="text-left font-medium text-muted-foreground">
                      Units
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
                      <TableCell className="font-medium text-foreground">{data.cityName}</TableCell>
                      <TableCell className="text-left font-mono tabular-nums text-emerald-400">
                        ${data.totalAmount.toLocaleString()}
                      </TableCell>
                      <TableCell className="text-left font-mono tabular-nums text-muted-foreground">
                        {data.totalUnits.toLocaleString()}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>
        <Card className="shadow-sm border-border/60 bg-card">
          <CardHeader className="pb-4">
            <CardTitle className="text-lg text-foreground">Top Salesman</CardTitle>
            <CardDescription>
              Ranked by total revenue amount
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="rounded-lg border border-border overflow-hidden bg-card">
              <Table>
                <TableHeader>
                  <TableRow className="bg-muted/40 hover:bg-muted/40 border-border">
                    <TableHead className="w-16 font-medium text-muted-foreground">Rank</TableHead>
                    <TableHead className="font-medium text-muted-foreground">Name</TableHead>
                    <TableHead className="text-left font-medium text-muted-foreground">Amount</TableHead>
                    <TableHead className="text-left font-medium text-muted-foreground">Units</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {salesmanData.map((data, index) => (
                    <TableRow key={index} className="hover:bg-muted/20 transition-colors border-border">
                      <TableCell className="font-medium text-primary">#{index + 1}</TableCell>
                      <TableCell className="font-medium text-foreground">{data.salesmanName}</TableCell>
                      <TableCell className="text-left font-mono tabular-nums text-emerald-400">
                        ${data.totalAmount.toLocaleString()}
                      </TableCell>
                      <TableCell className="text-left font-mono tabular-nums text-muted-foreground">
                        {data.totalUnits.toLocaleString()}
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
    </div>
  )
}

export default App
