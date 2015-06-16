# scala-http-server
Small just for fun project to dig deeper into the HTTP 1.1 and fast-cgi protocols.

What is working?

- [x] multithreaded processing of incoming requests
- [x] registry with different handlers (transform request to response) 
- [x] simple request matching (based on location or host etc.)
- [x] Handler to server static files from disk
- [x] simple registry to set correct content-type for static files based on extension
- [x] FastCgiHandler to proxy requests via fast-cgi protocol (tested with php-fpm) 
- [ ] keep-alive for connections to fast-cgi proxy
- [x] chunked transport encoding
- [x] gzip content encoding
- [x] support for HTTP 1.1 keep-alive
- [ ] maybe introduce some kind of URL rewriting like apache and nginx offer?
- [ ] support for conditional GET requests (If-Modified-Since header) and corresponding 304 responses
- [ ] more tests

So far it looks like the performance is not too bad in comparison with apache2 or nginx for example. Maybe at some point I will put together some benchmarks.
