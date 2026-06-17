# Tree-based data structures: Fenwick Tree

## 1. Fenwick essencial information

### What is a Fenwick Tree?
- A Fenwick Tree, also called a Binary Indexed Tree, is a data structure for arrays that change often.
- It stores partial sums in a compact way, so we do not need to recalculate totals from scratch.
- Each node is responsible for a range of values, and that range is determined by the lowest set bit of the index.
- Both updates and prefix-sum queries run in `O(log n)` time.
- It is a strong fit for streaming data with a fixed number of buckets.

## 2. Use Case

### Stock prices

A Fenwick Tree helps market software answer questions about data that is constantly changing.

Imagine a stock trading system that receives trades every millisecond. For each time bucket, we store:

- `volume`: how many shares traded in that bucket
- `price * volume`: the dollar value traded in that bucket

With those arrays, the system needs to answer questions like:

- What was the total volume between 10:00 and 10:05?

Without a Fenwick Tree, each question may require scanning many buckets. That is fine for a small demo, but not for a real-time order book with high-frequency data changes.

With a Fenwick Tree, each update and query is logarithmic:

- When a new trade arrives, update the bucket for its timestamp.
- When an order is added or canceled, update the bucket for its price level.
- When the system needs a range total, calculate two prefix sums and subtract them.

For example, if bucket `1` is `10:00`, bucket `2` is `10:01`, and bucket `3` is `10:02`, we can store the traded volume for each minute:

```text
volumeTree.add(timeBucket, tradeVolume)

volumeBetween10h00And10h05 =
  volumeTree.sum(bucket10h05) - volumeTree.sum(bucket10h00 - 1)
```

This is why Fenwick Trees appear in trading infrastructure discussions. A limit order book has to process add, cancel, and execute operations quickly, while also answering questions such as how much volume exists between two prices. Some on-chain order book designs also use Fenwick Trees inside a price level to track live order sizes and queue position efficiently.

It is best used for totals over time ranges or price ranges: volume, notional value, fees, imbalance, or liquidity depth. It is not a forecasting model and it is not a replacement for statistical analysis.

## 3. Fenwick tree deep dive

### Lowest set bit

- The lowest set bit is the rightmost `1` in a binary number.
- Example: `12` is `1100` in binary, so its lowest set bit is `4` (`0100` in binary).

### Query

- To compute the sum up to an index:
  - Accumulate the range sum stored at the current node
  - Move to the previous range by subtracting the lowest set bit
  - Repeat until the index becomes zero
- This produces a prefix sum in `O(log n)` time.

### Update

- To update one element, we add the delta to the current position.
- Then we move to the next affected ranges by adding the lowest set bit.
- Every node that covers that element gets updated.
- This also runs in `O(log n)` time.

## 4. Implementation code

Check how we implemented [FenwickTree in Kotlin](emersons-solution/src/FenwickTree.kt)

## 5. Benchmark

### 1. Comparison with some other data structure
### 2. Pros and Cons

#### Pros  
1. O(log n) time complexity for query
2. Simple to implement
3. No lazy propagation needed

#### Cons  
1. Fixed size: the size of the Fenwick Tree must be defined at initialization and cannot be changed dynamically.
2. Designed to prefix sums
3. Mandatory 1-based indexing
4. Range update not natively supported
5. O(log n) time complexity for updates  


# 6. References

- https://medium.com/@kanishks772/the-hidden-architecture-of-the-internet-20-algorithms-that-power-everything-9e0d139a9bd0
- https://arxiv.org/html/2304.02356v3
- https://medium.com/@francescofranco_39234/fenwick-trees-4310799f68e2
- https://cp-algorithms.com/data_structures/fenwick.html
- https://stackoverflow.com/questions/77217104/simpler-alternatives-to-fenwick-trees
- https://www.shadecoder.com/de/topics/what-is-fenwick-tree-bit-a-practical-guide-for-2025
- https://www.investopedia.com/terms/b/binomialoptionpricing.asp#toc-applying-the-binomial-option-pricing-model-in-real-trading
- https://github.com/Crypto-toolbox/HFT-Orderbook
- https://quant.stackexchange.com/questions/63140/red-black-trees-for-limit-order-book
- https://stackoverflow.com/questions/21995930/dynamic-i-e-variable-size-fenwick-tree
- https://medium.com/@0xape/binary-indexed-trees-a-beginner-friendly-visual-guide-15fc1d77cad1
- https://ethresear.ch/t/fenwick-bitmap-constant-time-matching-for-on-chain-order-books/22313
- https://reports.chainsecurity.com/Pendle/ChainSecurity_Pendle_BorosMarkets_Audit.pdf
- https://quant.stackexchange.com/questions/80117/efficiently-tracking-order-queue-position-in-a-limit-order-book-implementation
