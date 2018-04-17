<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Serviço de Autenticação</title>
    <link rel="stylesheet" href="auth/css/styles.css">
</head>
    <body>
        <?php
        $dbconn = pg_connect("host=db port=5432 dbname=login user=postgresql password=postgresql");
        if(isset($_GET['token']) && $_GET['token'] != "") {
            $results = pg_query($dbconn, "SELECT * FROM users WHERE token='".$_GET['token']."'");
            if(pg_num_rows($results) === 1) {
                echo 1;
                exit;
            }
            else {
                echo 0;
                exit;
            }
        }
         ?>
        <div class="nav">
            <div class="row">
                <ul>
                    <li class="service"><a href='http://localhost:8080:8080/auth'>Serviço de Autenticação</a></li>
                    <li><a onclick='registo()'>Register</a></li>
                    <li><a onclick='login()'>Login</a></li>
                </ul>
            </div>
        </div>
        <div class="row">
            <div class="content">
                <?php
                if(!isset($_COOKIE['token'])) {
                    ?>
                    <div class="login-page">
                      <div class="form">
                        <form class="register-form" method="post">
                          <input type="text" name="user" placeholder="Username"/>
                          <input type="password" name="pass" placeholder="Password"/>
                          <input type="text" hidden name="email" value="1">
                          <button>create</button>
                          <span></span>
                          <p class="message">Already registered? <a href="#">Sign In</a></p>
                        </form>
                        <form class="login-form" method="post">
                          <input type="text" name="user" placeholder="Username"/>
                          <input type="password" name="pass" placeholder="Password"/>
                          <button>login</button>
                          <span></span>
                          <p class="message">Not registered? <a href="#">Create an account</a></p>
                        </form>
                      </div>
                    </div>
                    <?php
                }
                else {
                    $print = 0;
                    if($_COOKIE['token'] != "") {
                        $results = pg_query($dbconn, "SELECT * FROM users WHERE token='".$_COOKIE['token']."'");
                        if(pg_num_rows($results) === 1) {
                            echo "<h1>Já tem sessão iniciada. Irá ser redirecionado.</h1>";
                            ?>
                            <input type="text"  hidden class="redirect" value="1">
                            <?php
                        }
                        else {
                            $print = 1;
                        }
                    }
                    else {
                        $print = 1;
                    }
                }
                if($print === 1) {
                    ?>
                    <div class="login-page">
                      <div class="form">
                        <form class="register-form" method="post">
                          <input type="text" name="user" placeholder="Username"/>
                          <input type="password" name="pass" placeholder="Password"/>
                          <input type="text" hidden name="email" value="1">
                          <button>create</button>
                          <span></span>
                          <p class="message">Already registered? <a href="#">Sign In</a></p>
                        </form>
                        <form class="login-form" method="post">
                          <input type="text" name="user" placeholder="Username"/>
                          <input type="password" name="pass" placeholder="Password"/>
                          <button>login</button>
                          <span></span>
                          <p class="message">Not registered? <a href="#">Create an account</a></p>
                        </form>
                      </div>
                    </div>
                    <?php
                }
                ?>
            </div>
        </div>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
        <script src="auth/js/cookies.js"></script>
        <?php
        if($print == 1) {
            ?>
            <script type="text/javascript">
                setCookie('token', '', -1);
            </script>
            <?php
        }
        ?>
        <script type="text/javascript">
            var show = 1;
            if($('.redirect').val()) {
                setTimeout(function () {
                    location.href="http://localhost:8080/mail";
                }, 2000);
            }

            function registo() {
                if(show % 2 != 0) {
                    $('form').animate({height: "toggle", opacity: "toggle"}, "slow");
                    if(show != 1) {
                        $('.login-form span').html("");
                        $('.login-form span').css('color', 'red');
                        $('.login-form button').css('margin-bottom', '0px');
                        $('.register-form span').html("");
                        $('.register-form span').css('color', 'red');
                        $('.register-form button').css('margin-bottom', '0px');
                    }
                    show++;
                }
            }

            function login() {
                if(show % 2 == 0) {
                    $('form').animate({height: "toggle", opacity: "toggle"}, "slow");
                    $('.login-form span').html("");
                    $('.login-form span').css('color', 'red');
                    $('.login-form button').css('margin-bottom', '0px');
                    $('.register-form span').html("");
                    $('.register-form span').css('color', 'red');
                    $('.register-form button').css('margin-bottom', '0px');
                    show++;
                }
            }

            $('.message a').click(function(){
                $('form').animate({height: "toggle", opacity: "toggle"}, "slow");
                $('.login-form span').html("");
                $('.login-form span').css('color', 'red');
                $('.login-form button').css('margin-bottom', '0px');
                $('.register-form span').html("");
                $('.register-form span').css('color', 'red');
                $('.register-form button').css('margin-bottom', '0px');
                show++;
            });
        </script>
        <?php
        if(isset($_POST)) {
            if(isset($_POST['email']) && $_POST['user'] != "" && $_POST['pass'] != "") {
                $results = pg_query($dbconn, "SELECT * FROM users WHERE username='".$_POST['user']."'");
                if(pg_num_rows($results) === 0) {
                    $results = pg_query($dbconn, "SELECT * FROM users ORDER BY iduser DESC LIMIT 1");
                    $row = pg_fetch_row($results);
                    $token = bin2hex(random_bytes(32));
                    pg_query($dbconn, "INSERT INTO Users VALUES(".($row[0]+1).", '".$token."', '".$_POST['pass']."', '".$_POST['user']."')");
                    ?>
                    <script type="text/javascript">
                        setCookie('token', '<?php echo $token; ?>', 100);
                        $('.register-form span').html("Registered successfully!<br>You're being redirected!");
                        $('.register-form span').css('color', 'green');
                        $('.register-form button').css('margin-bottom', '10px');
                        registo();
                        setTimeout(function () {
                            location.href="http://localhost:8080/mail";
                        }, 2000);
                    </script>
                    <?php
                }
                else {
                    ?>
                    <script type="text/javascript">
                        $('.register-form span').html("That username already exists!<br>Choose another one!");
                        $('.register-form span').css('color', 'red');
                        $('.register-form button').css('margin-bottom', '10px');
                        registo();
                    </script>
                    <?php
                }
            }
            else if ($_POST['user'] != "" && $_POST['pass'] != "") {
                $results = pg_query($dbconn, "SELECT * FROM users WHERE username='".$_POST['user']."' AND password='".$_POST['pass']."'");
                if(pg_num_rows($results) === 1) {
                    $token = bin2hex(random_bytes(32));
                    pg_query($dbconn, "UPDATE users SET token='".$token."' WHERE username='".$_POST['user']."'");
                    ?>
                    <script type="text/javascript">
                        setCookie('token', <?php echo "'".$token."'"; ?>, 2);
                        $('.login-form span').html("Logged in successfully!");
                        $('.login-form span').css('color', 'green');
                        $('.login-form button').css('margin-bottom', '10px');
                        setTimeout(function () {
                            location.href="http://localhost:8080/mail";
                        }, 2000);
                    </script>
                    <?php
                }
                else {
                    ?>
                    <script type="text/javascript">
                        $('.login-form span').html("Username and/or password are wrong!");
                        $('.login-form span').css('color', 'red');
                        $('.login-form button').css('margin-bottom', '10px');
                    </script>
                    <?php
                }
            }
        }
         ?>
    </body>
</html>
