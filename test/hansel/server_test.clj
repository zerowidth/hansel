(ns hansel.server-test
  (:use midje.sweet
        hansel.server))

(fact
  "filter-for-presentation filters unused keys from a step"
  (filter-for-presentation identity {:foo "foo" :bar "bar"
                                     :open [] :closed []
                                     :current nil :paths []}) =>
    {:open [] :closed [] :current nil :paths []})

(fact
  "filter-for-presentation with a 'first' pos-fn filters the first of the open nodes"
  (set
    (:open (filter-for-presentation
             first
             {:open #{[[1 1] [0 0]] [[2 2] [3 3]]}}))) => #{[1 1] [2 2]})

(fact
  "filter-for-presentation with a 'first' pos-fn filters the first of the closed nodes"
  (set
    (:closed (filter-for-presentation
               first
               {:closed #{[[1 1] [0 0]] [[2 2] [3 3]]}}))) => #{[1 1] [2 2]})

(fact
  "filter-for-presentation with a 'first' pos-fn filters the first of the paths"
  (vec (:paths (filter-for-presentation
                 first
                 {:paths [ [[[1 1] [0 0]] [[2 2] [3 3]]] ]}))) => [[[1 1] [2 2]]])

(fact
  "filter-for-presentation with a 'first' pos-fn filters the first of the current node"
  (:current (filter-for-presentation first {:current [[1 1] [0 0]]})) => [1 1])

