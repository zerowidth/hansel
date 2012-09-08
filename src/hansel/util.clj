(ns hansel.util)

(defn path [state]
  "Return the calculated path given a state. Returns a sequence of nodes if
  there is a path from start to dest, nil if no path exists."
  (let [steps (take-while identity (iterate (:parents state) (:dest state)))]
    (if ((set steps) (:start state))
      (reverse steps))))
