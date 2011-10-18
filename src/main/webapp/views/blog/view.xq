import module namespace web="http://basex.org/lib/web";
let $entry := blog:find($GET('entry'))
let $comments := $entry/comments
return

<div class="post detail">
{web:flash()}

<h1>{$entry/title/text()}</h1>


    <div class="post">
        <h3>{$entry/title/text()} <small>{" ", data($entry/@date)}</small></h3>
        <p>{$entry/body/text()}
        </p>
        <div class="comments">
        {
            if(count($comments)) then 
                for $comment in $comments//comment
                return 
                <div class="comment">
                    <div class="meta">
                    <h5>{$comment/from/text()}</h5> writes on {string($comment/@date)}</div>
                    <p>{$comment/body}</p>
                    
                </div>
            else "No comments yet, be the first to write one!"
        }{
            blog:comment-box($GET('entry'))
        }
        </div>
        <hr />
        <a href="/app/blog">← Back</a>
    </div>
</div>