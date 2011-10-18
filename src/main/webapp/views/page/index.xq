import module namespace web="http://basex.org/lib/web";
<div>
<h1>Hello World</h1>
    <p>Welcome to basex-web.</p>
    <h2>Project goals</h2>
    <p>Emerging from a database context, the main contribution of this work is to provide implementors and software architects with a development stack, </p>
    <p>consisting of an application server and an optional software framework written in XQuery, that is built with XML technologies from front to back.</p>
    <p>Suggested approach not only takes away the need to master a plenitude of technologies, but also, tackles some of the shortcomings relational database management systems face.</p>
    <h3>Core Componenets</h3>
    <dl>
      <dt>Models</dt>
      <dd>…</dd>
      <dt>Views</dt>
      <dd>…</dd>
      <dt>Controllers</dt>
      <dd>…</dd>
    </dl>
    
    
    
    <h2>Mini Example</h2>
    <p>The following list is generated dynamically by <a href="http://basex.org" target="_blank">BaseX</a>:</p>
    <ul>
    {for $x in 1 to 10 
     return <li>{$x}</li>
    }</ul>
   <p> Source for the above list:
    <listing>&lt;ul&gt;
    {{
      for $x in 1 to 10 
      return &lt;li&gt;{{$x}}&lt;/li&gt;
}}&lt;/ul&gt;
    </listing>
    
    </p>
 </div>