import module namespace web = "http://basex.org/lib/web";
declare option output:omit-xml-declaration "no";
declare option output:method "xhtml";
declare option output:include-content-type "yes";
declare option output:doctype-public "-//W3C//DTD XHTML 1.1//EN";
declare option output:doctype-system "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd";
 
<html xmlns="http://www.w3.org/1999/xhtml">
<head>	
	<title>Pure XQuery in your Browser </title>
	<meta name="description" content="This is a demo of a pure XQuery website"/>
	<meta name="author" content="Michael Seiferle" />
	
	<meta name="viewport" content="width=device-width, initial-scale=1.0" />
	<script src="js/libs/modernizr-1.7.min.js" type="text/javascript">/* can't touch this! */</script>
	<link rel="shortcut icon" href="/favicon.ico" />
	<link rel="apple-touch-icon" href="/apple-touch-icon.png" />
	<link type="text/css" rel="stylesheet" href="css/style.css?v=2" />
	<link type="text/css" rel="stylesheet" media="handheld" href="css/handheld.css?v=2" />

</head>
<body>
<div id="container">
	<header>
    <h1>Pure XQuery in your Browser</h1>
	</header>
	<div id="main" role="main">
  
    <article class="example">
  <h2>Example</h2>
  <p>The following list is generated by XQuery, please make sure to have a look at the source file:</p>
  <br />
  <ul>{
  for $x in 1 to 10
  return <li>Lists {$x}</li>
}</ul>
<p>{
  web:set-cookie("hello", "world",xs:int(5),"/") ,
  "My cookie says: ", web:get-cookie("hello") 
  
}
</p>
</article>
<hr />
<a href="/index.html">&lt; Please Click here to return to the previous page.</a>
<hr />
<h2>Looking for XQuery?</h2>
...this is what the actual source looks like: 
<a href="source.xq.html">index.xq.html &gt;</a>

</div>
</div>
</body>
</html>
