<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <title>Serviço de Email</title>
        <link rel="stylesheet" href="mail/css/styles.css">
    </head>
    <body>
        <?php
        if(!isset($_COOKIE['token']) || $_COOKIE['token'] == "") {
            ?>
            <script type="text/javascript">
                location.href="http://localhost:8080/auth";
            </script>
            <?php
            exit;
        }
        $curl_handle = curl_init();
        $tok = array('token' => "".$_COOKIE['token']);
        $url = "http://auth/";
        $defaults = array(
            CURLOPT_URL => $url. (strpos($url, '?') === FALSE ? '?' : ''). http_build_query($tok),
            CURLOPT_HEADER => 0,
            CURLOPT_RETURNTRANSFER => TRUE
        );
        curl_setopt_array($curl_handle, $defaults);
        $html = curl_exec( $curl_handle ); // Execute the request
        curl_close( $curl_handle );
        if($html=='0') {
            ?>
            <script type="text/javascript">
                location.href="http://localhost:8080/auth";
            </script>
            <?php
        }
        ?>
        <div class="nav">
            <div class="row">
                <ul>
                    <li class="service"><a href='http://localhost:8080/mail'>Serviço de Email</a></li>
                    <?php
                    $dbconn = pg_connect("host=db port=5432 dbname=login user=postgresql password=postgresql");
                    $results = pg_query($dbconn, "SELECT * FROM users WHERE token='".$_COOKIE['token']."'");
                    $row = pg_fetch_row($results);
                    echo "<li><a onclick='logout()'>Logout</a></li>";
                    echo "<li><a class='user'>Olá, ".$row[3]."!</a></li>";
                    ?>
                </ul>
            </div>
        </div>
        <div class="row">
            <div class="content">
                <div class="email-page">
                    <div class="form">
                        <form class="login-form" method="post">
                            <h1>Send Email</h1>
                            <input class="ass" type="text" name="assunto" placeholder="Subject:"/>
                            <input class="dest" type="text" name="dest" placeholder="TO: (comma separated)"/>
                            <input class="cc" type="text" name="cc" placeholder="CC: (comma separated)"/>
                            <input class="bcc" type="text" name="bcc" placeholder="BCC: (comma separated)"/>
                            <textarea class="msg" name="msg" rows="8" cols="80" placeholder="Message"></textarea>
                            <button>send</button>
                            <span></span>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
        <script src="mail/js/cookies.js"></script>
        <script type="text/javascript">
            function logout() {
                setCookie('token', '', -1);
                location.href="http://localhost:8080/auth";
            }
        </script>
        <?php
        use PHPMailer\PHPMailer\PHPMailer;
        if (isset($_POST) && $_POST['dest'] != "" && $_POST['msg'] != "") {
            require "vendor/autoload.php";
            $mail = new PHPMailer;
            $mail->isSMTP();
            $mail->SMTPDebug = 0;
            $mail->Host = 'smtp';
            $mail->Port = 25;
            $mail->SMTPAuth = false;
            $mail->setFrom('vr-g4@gcom.di.uminho.pt', 'Grupo 4');
            $mail->addReplyTo('vr-g4@gcom.di.uminho.pt', 'Grupo 4');
            $address = explode(",", $_POST['dest']);
            for($i = 0; $i < count($address); $i++) {
                $mail->addAddress($address[$i], '');
            }
            if(isset($_POST['cc']) && $_POST['cc'] != "") {
                $address = explode(",", $_POST['cc']);
                for($i = 0; $i < count($address); $i++) {
                    $mail->addCC($address[$i], '');
                }
            }
            if(isset($_POST['bcc']) && $_POST['bcc'] != "") {
                $address = explode(",", $_POST['bcc']);
                for($i = 0; $i < count($address); $i++) {
                    $mail->addBCC($address[$i], '');
                }
            }
            $mail->Subject = $_POST['assunto'];
            $mail->msgHTML("
            <html>
                <body>
                ".$_POST['msg']."
                </body>
            </html>");
            $mail->AltBody = $_POST['msg'];
            if (!$mail->send()) {
                ?>
                <script type="text/javascript">
                    $('.login-form ass').val('<?php echo $_POST['assunto']; ?>');
                    $('.login-form dest').val('<?php echo $_POST['dest']; ?>');
                    $('.login-form cc').val('<?php echo $_POST['cc']; ?>');
                    $('.login-form bcc').val('<?php echo $_POST['bcc']; ?>');
                    $('.login-form msg').val('<?php echo $_POST['msg']; ?>');
                    $('.login-form span').html("Error while sending the message!");
                    $('.login-form span').css("color", "red");
                    $('.login-form button').css("margin-bottom", "10px");
                    setTimeout(function () {
                        $('.login-form span').html("");
                        $('.login-form button').css("margin-bottom", "0px");
                    }, 2000);
                </script>
                <?php
            } else {
                ?>
                <script type="text/javascript">
                    $('.login-form span').html("Message was sent successfully!");
                    $('.login-form span').css("color", "green");
                    $('.login-form button').css("margin-bottom", "10px");
                    setTimeout(function () {
                        $('.login-form span').html("");
                        $('.login-form button').css("margin-bottom", "0px");
                    }, 2000);
                </script>
                <?php
            }
        }
         ?>
    </body>
</html>
