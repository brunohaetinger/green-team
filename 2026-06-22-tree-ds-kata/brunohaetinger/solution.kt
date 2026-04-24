// Solution -TBD

// bruno notes:
/*
array: A
fenwick array: T

// array index starting in 1
// array index defined as binary numbers

T stores values from A if the index last binary index number is 1.
e.g.: element on index 3 => 00011 => right most bit is 1 => then store its value on T

if second most bit is 1, then sum(A[i]} + A[i-1]) 
e.g.: index 2 => 00010 => T[2] = A[2] + A[1]


if the third bit is 1, then sum last 4 values: sum(A[i-3...i])


int sum(int i){
  int sum = 0;
  while(i > 0) {
    sum += T[i];
    i -= i & -i; //flip the last set bit
  }
  return sum;
}


int[] make(int[] ar){
  int[] tree = Arrays.copyOf(ar, ar.length);

  for(int i=1; i<tree.length; i++){
    int p = i + (i & -i); // index to parent
    if(p < t.length) {
      tree[p] = tree[p] + tree[i];
    }
  }
  return tree;
}


// -i in binary:
1. Write i in binary
2. Invert all bits
3. Add 1

e.g:
001100 = 12
110011 = all bits inverted
+1
= 110100

*/
