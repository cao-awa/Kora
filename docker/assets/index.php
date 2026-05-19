<?php

ob_clean();

echo '<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>CGI Connection test</title></head>
<body>
<h1>CGI Connection test success</h1>
<hr>
<h2>Server information</h2>
<ul>';

$cgi_vars = [
    'SERVER_SOFTWARE'   => 'Web server software',
    'GATEWAY_INTERFACE' => 'CGI Version',
    'REQUEST_METHOD'    => 'Request method',
    'QUERY_STRING'      => 'Query string',
    'SCRIPT_NAME'       => 'Script name',
    'PHP_SELF'          => 'PHP Script path',
];

foreach ($cgi_vars as $key => $desc) {
    $value = $_SERVER[$key] ?? 'No settings';
    echo "<li><strong>{$desc}：</strong> {$value}</li>";
}

echo '<li><strong>PHP Version: </strong> ' . PHP_VERSION . '</li>';
echo '</ul>';

echo '<details>
<summary>Click to view all $_SERVER Variables</summary>
<pre>';
print_r($_SERVER);
echo '</pre>
</details>';

echo '</body></html>';