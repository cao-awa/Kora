<?php

ob_clean();

// header('Content-Type: text/html; charset=utf-8');

echo '<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>CGI 连接测试</title></head>
<body>
<h1>CGI 连接测试成功！</h1>
<hr>
<h2>服务器信息</h2>
<ul>';

$cgi_vars = [
    'SERVER_SOFTWARE'   => 'Web 服务器软件',
    'GATEWAY_INTERFACE' => 'CGI 版本',
    'REQUEST_METHOD'    => '请求方法',
    'QUERY_STRING'      => '查询字符串',
    'SCRIPT_NAME'       => '脚本名称',
    'PHP_SELF'          => 'PHP 脚本路径',
];

foreach ($cgi_vars as $key => $desc) {
    $value = $_SERVER[$key] ?? '未设置';
    echo "<li><strong>{$desc}：</strong> {$value}</li>";
}

// 显示 PHP 版本（作为额外确认）
echo '<li><strong>PHP 版本：</strong> ' . PHP_VERSION . '</li>';
echo '</ul>';

// 可选：显示完整的 $_SERVER 信息（调试用）
echo '<details>
<summary>点击查看全部 $_SERVER 变量</summary>
<pre>';
print_r($_SERVER);
echo '</pre>
</details>';

echo '</body></html>';