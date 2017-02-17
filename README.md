# Clojure Code Poster

Generate beautiful code posters in Clojure. Inspired (heavily) by the [Build
Your Own Code Poster With Elixir](http://www.east5th.co/blog/2017/02/13/build-your-own-code-poster-with-elixir/) blogpost.

## Usage

You will need an image file and a code file. The SVG will be rendered using the
Adove Source Code Pro fonts, so you will also need to have these installed in
your system.

Please note that due to the font ratio you will have to adjust the image
beforehand. Change the width to be ~166.7% of the height. Then resize your image
(preserving the new ratio) so that height is 300px.

Then run (this might take a while):

``` shell
$ java -jar clj-code-poster-0.1.0-standalone.jar -i image.png -c code.clj -o out.svg

```

and enjoy your newly created Code Poster :)

## TODO

- [ ] Write a blog post.
- [ ] Rescale the image inside the application.
- [ ] Examine whether SVG rendering can be optimized.
- [ ] Add option to produce PNG image.

## License

Copyright © 2017 Nikos Fertakis

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
