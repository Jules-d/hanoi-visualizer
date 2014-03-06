# hanoi-visualizer

A [Heroku](http://www.heroku.com) web app using Compojure.

This is a visualizer for solutions in haskel from the Yorgey lectures http://www.seas.upenn.edu/~cis194/lectures/01-intro.html
Use the url to pass in your haskell solution, e.g.:
http://localhost:3000/[("a","c"), ("a","b"), ("c","b")]
or http://localhost:3000/[("a","b"),("a","c"),("b","c"),("a","b"),("c","a"),("c","b"),("a","b")]
It should also work with more pegs
Each line is one step in your solution, and it's supposed to be ascii art as though you're looking down on the pegs from above.

## License

Copyright © 2014 Julian de Bhal

Distributed under the Eclipse Public License, the same as Clojure.
