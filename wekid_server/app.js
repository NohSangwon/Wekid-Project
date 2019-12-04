var express = require('express');
var bodyParser = require('body-parser');
var app = express();
var mysql = require('mysql');
var connection = mysql.createConnection({
  host: '127.0.0.1',
  port: '3306',
  user: 'root',
  password: 'shtkddnjs1!',
  database: 'system_project'
});
connection.connect();
//app.use(bodyParser.urlencoded({extended: true}));
//app.use(bodyParser.json());
app.use(bodyParser.json({ limit: '20mb' }));
app.use(bodyParser.urlencoded({ extended: true, limit: '20mb' }));

//파일관련 모듈
var multer = require('multer')

//파일 저장위치와 파일이름 설정
var storage = multer.diskStorage({
  destination: function (req, file, cb) {
    //파일이 이미지 파일이면
    if (file.mimetype == "image/jpeg" || file.mimetype == "image/jpg" || file.mimetype == "image/png") {
      console.log("이미지 파일이네요")
      cb(null, 'uploads/images')
      //텍스트 파일이면
    } else if (file.mimetype == "application/pdf" || file.mimetype == "application/txt" || file.mimetype == "application/doc" || file.mimetype == "application/docx" || file.mimetype == "application/hwp" || file.mimetype == "application/octet-stream") {
      console.log("텍스트 파일이네요")
      cb(null, 'uploads/texts')
    }
  },

  //파일이름 설정
  filename: function (req, file, cb) {
    cb(null, Date.now() + "-" + file.originalname)
  }
})

//파일 업로드 모듈
var upload = multer({ storage: storage })

//<------ 로그인 하는 라우터 ------>///
app.post('/login', function(req, res){
  var inputId = req.body.inputId;
  var inputPwd = req.body.inputPwd;
  var checkUserType = req.body.checkUserType;
  console.log(inputId);
  console.log(inputPwd);

// 학부모로 로그인 했을 경우
if(checkUserType == 1) {
  var stmt_duplicated = 'select id, TB_PARENTS.name, phoneNum, TB_KIDS.kinderCode, TB_KIDS.classCode from `TB_PARENTS`'
 + ' JOIN TB_KIDS ON TB_KIDS.parentsId = TB_PARENTS.id'
 + ' where TB_PARENTS.id = ? and TB_PARENTS.pwd = ?';

  connection.query(stmt_duplicated, [inputId, inputPwd], function(err,rows) {
    console.log("Checking...1");
    var status = rows.length; // 연결됐을 경우 1, 안됐을 경우 0이 저장됨
    console.log("Checking...: " + status);

    if(err) {
    console.log(err);
      return done(err);
    } else {
      if(status == 0) { // 로그인 실패
        console.log(status);
        res.json({status:status});  // 안드로이드로 status = 0을 보냄(로그인 실패할 경우 status = 0)
      } else if(status > 0) { // 로그인 성공
        console.log("학부모 : " + rows[0].id + "가 로그인했습니다.");

    var resultArray = new Array();
    for (var i = 0; i < rows.length; i++) {

      var resultObj = {
        "kinderCode" : rows[i].kinderCode,
        "classCode" : rows[i].classCode
      }

      resultArray.push(resultObj);
    }

        res.json({status:1, id:rows[0].id, name:rows[0].name, phoneNum:rows[0].phoneNum, parentsKinderInfo: resultArray});
      }
    }
  }) // 교사로 로그인 했을 경우
}else if(checkUserType == 0) {

    var stmt_duplicated = 'SELECT TB_TEACHER.id, TB_TEACHER.name, TB_TEACHER.phoneNum, TB_Kinder.kinderName, TB_class.className, TB_TEACHER.classCode, TB_TEACHER.kinderCode from `TB_TEACHER`'
+ ' JOIN TB_KINDER ON TB_TEACHER.kinderCode = TB_KINDER.kinderCode'
+ ' JOIN TB_CLASS ON TB_TEACHER.classCode = TB_CLASS.classCode and TB_TEACHER.kinderCode=tb_class.kinderCode '
+ ' WHERE `id` = ? and `pwd` = ?';

    connection.query(stmt_duplicated, [inputId, inputPwd], function(err,rows) {
      var status = rows.length; // 연결됐을 경우는 담당하고 있는 학생의 수, 연결 안됐을 경우 0이 저장됨

      if(err) {
        return done(err);
      } else {
        if(status == 0) {
          console.log(status);
          res.json({status:status});  // 안드로이드로 status = 0을 보냄(로그인 실패할 경우 status = 0)
        } else if(status == 1) {
           res.json({status:status, id:rows[0].id, name:rows[0].name, kinderName:rows[0].kinderName, className:rows[0].className, phoneNum:rows[0].phoneNum, kinderCode:rows[0].kinderCode, classCode:rows[0].classCode});
        }
      }
    })// 원장으로 로그인 했을 경우
  } else if(checkUserType == 2) {
    var stmt_duplicated = 'SELECT TB_PRINCIPAL.id, TB_PRINCIPAL.name, TB_PRINCIPAL.phoneNum, TB_PRINCIPAL.kinderCode, TB_Kinder.kinderName from `TB_PRINCIPAL`'
    + ' JOIN TB_KINDER ON TB_PRINCIPAL.kinderCode = TB_KINDER.kinderCode'
    + ' WHERE `id` = ? and `pwd` = ?';

    connection.query(stmt_duplicated, [inputId, inputPwd], function(err,rows) {
      var status = rows.length; // 연결됐을 경우 1, 안됐을 경우 0이 저장됨

      if(err) {

        return done(err);
      } else {
        if(status == 0) {
          console.log(status);
          res.json({status:status});  // 안드로이드로 status = 0을 보냄(로그인 실패할 경우 status = 0)
        } else if(status == 1) {
          console.log(status);
          res.json({status:status, id:rows[0].id, name:rows[0].name, kinderCode:rows[0].kinderCode, kinderName:rows[0].kinderName, phoneNum:rows[0].phoneNum});
        }
      }
    })
  }
})

//<------ 여기까지 ------>//

//<------ 학부모 id를 받아서 자식 정보 가져오는 라우터 ------>//
app.post('/getKidsListFromParents', function(req, res){
  var id = req.body.id;
  console.log("학부모 id : " + id);

  var stmt_duplicated = 'SELECT  TB_kids.name, TB_kids.birth, TB_kids.address,  TB_kids.parentsId, TB_kinder.kinderName, TB_class.className, TB_kids.kinderCode from `TB_KIDS`'
 + ' JOIN TB_KINDER ON TB_KIDS.kinderCode = TB_KINDER.kinderCode'
 + ' JOIN TB_CLASS ON TB_KIDS.classCode = TB_CLASS.classCode and tb_kids.kinderCode=tb_class.kinderCode '
 + ' WHERE `parentsId` = ? ';

  connection.query(stmt_duplicated, [id], function(err,rows) {
    console.log(rows);
    console.log(rows.length);
    var status = rows.length; // 자식 명수가 저장됨, 없으면 무조건 0

    if(err) {
      return done(err);
    } else {
      if(status == 0) {
        console.log(status);
        res.json({status:status});  // 안드로이드로 status = 0을 보냄
      } else {
        for(var i = 0; i < status; i++) {
          console.log("자식 : " + rows[i].name);
        }

        res.json({status:status, rows:rows});
      }
    }
  })
});
//<------ 여기까지 ------>//
//<------ 여기까지 ------>//

//<------ 교사 class code를 받아서 담당 원아 정보 가져오는 라우터 ------>//
app.post('/getKidsListFromTeacher', function(req, res){
  var id = req.body.id;
  console.log("교사 id : " + id);


  var stmt = 'SELECT name, birth, address, parentsId, kinderCode, classCode FROM tb_kids WHERE  kinderCode=(SELECT kinderCode  FROM `TB_TEACHER` WHERE `id` = ?)'
  +'and classCode=(SELECT  classCode FROM `TB_TEACHER` WHERE `id` = ?)'
  connection.query(stmt, [id,id], function(err,rows) {
    console.log(rows);
    console.log(rows.length);
    var status = rows.length; // 담당 원아 명수가 저장됨, 없으면 무조건 0

    if(err) {
      return done(err);
    } else {
      if(status == 0) {
        console.log(status);
        res.json({status:status});  // 안드로이드로 status = 0을 보냄
      } else {
        for(var i = 0; i < status; i++) {
          console.log("담당 원아 : " + rows[i].name);
        }
        res.json({status:status, rows:rows});
      }
    }
  })
});
//<------ 여기까지 ------>//

//<------ 원장 kinderCode 를 받아서 교사 정보 가져오는 라우터 ------>//
app.post('/getTeacherList', function(req, res){
	console.log("***************************** getTeacherList *****************************");
  var kinderCode = req.body.kinderCode;
  console.log("원장 kinderCode : " + kinderCode);

  var stmt_duplicated = 'SELECT TB_TEACHER.id, TB_TEACHER.name, TB_TEACHER.kinderCode, TB_KINDER.kinderName, TB_TEACHER.classCode, TB_CLASS.className from `TB_TEACHER`'
 + ' JOIN TB_KINDER ON TB_TEACHER.kinderCode = TB_KINDER.kinderCode'
 + ' JOIN TB_CLASS ON TB_TEACHER.classCode = TB_CLASS.classCode and TB_TEACHER.kinderCode = TB_CLASS.kinderCode'
 + ' WHERE TB_TEACHER.kinderCode = ?';

  connection.query(stmt_duplicated, kinderCode, function(err,rows) {
    console.log(rows);
    console.log(rows.length);
    var status = rows.length; // 자식 명수가 저장됨, 없으면 무조건 0

    if(err) {
      return done(err);
    } else {
      if(status == 0) {
        console.log(status);
        res.json({status:status});  // 안드로이드로 status = 0을 보냄
      } else {
        for(var i = 0; i < status; i++) {
          console.log("자식 : " + rows[i].name);
        }
        res.json({status:status, result:rows});
      }
    }
  })
})
//<------ 여기까지 ------>//

//<------ 교사 id를 받아서 교사 classCode 를 업데이트하는 라우터 ------>//
app.post('/updateTeacher', function(req, res){
	console.log("***************************** updateTeacher *****************************");
  var id = req.body.id;
  var classCode = req.body.classCode;
  console.log("교사 id : " + id);
  console.log("새 classCode : " + classCode);

  var stmt_duplicated = 'UPDATE `TB_TEACHER`'
	 + ' SET classCode = ?'
	 + ' where id = ?';

  connection.query(stmt_duplicated, [classCode, id], function(err,rows) {
    var status = rows.length;

    if(err) {
      return done(err);
    } else {
      res.json({status:status});  // 안드로이드로 status = 0을 보냄
    }
  })
})
//<------ 여기까지 ------>//

//<-------- 회원가입 액션
app.post('/insertparent',function(req,res){

    var inputId = req.body.inputId;
    var inputPwd = req.body.inputPwd;
    var inputName = req.body.inputName;
    var inputPhoneNum = req.body.inputPhoneNum;
    var stmt_duplicated="SELECT id FROM (SELECT id FROM `TB_PARENTS` UNION SELECT id FROM `TB_PRINCIPAL` UNION SELECT id FROM `TB_TEACHER`) T1 where T1.id= ?";
    var stmt_duplicated2="INSERT INTO TB_PARENTS (id,pwd,name,phoneNum) VALUES(?,?,?,?)";


    connection.query(stmt_duplicated,inputId,(err,rs)=>{

        if(rs[0]){
            status='0';
            res.json({status:status});
        }else{

            connection.query(stmt_duplicated2,[inputId,inputPwd,inputName,inputPhoneNum] ,(err,rs)=>{

            status='1';
            res.json({status:status});

            });
        }
    });



});

app.post('/insertteacher',function(req,res){

    var inputId = req.body.inputId;
    var inputPwd = req.body.inputPwd;
    var inputName = req.body.inputName;
    var inputPhoneNum = req.body.inputPhoneNum;
    var kinderCode=req.body.kinderCode;

    console.log(kinderCode);
    var stmt_duplicated="SELECT id FROM (SELECT id FROM `TB_PARENTS` UNION SELECT id FROM `TB_PRINCIPAL` UNION SELECT id FROM `TB_TEACHER`) T1 where T1.id= ?";
    var stmt_duplicated2="INSERT INTO TB_TEACHER (id,pwd,name,phoneNum,kinderCode,classCode) VALUES(?,?,?,?,?,?)";

    connection.query(stmt_duplicated,inputId,(err,rs)=>{

        console.log(rs[0]);
        console.log(rs);
        if(rs[0]){
            status='0';
            res.json({status:status});
        }else{

            connection.query(stmt_duplicated2,[inputId,inputPwd,inputName,inputPhoneNum,kinderCode,"0"] ,(err,rs)=>{

            status='1';
            res.json({status:status});

            });
        }
    });



});

app.post('/insertprincipal',function(req,res){

    var inputId = req.body.inputId;
    var inputPwd = req.body.inputPwd;
    var inputName = req.body.inputName;
    var inputPhoneNum = req.body.inputPhoneNum;
    var kinderCode = req.body.kinderCode;
    var stmt_duplicated="SELECT id FROM (SELECT id FROM `TB_PARENTS` UNION SELECT id FROM `TB_PRINCIPAL` UNION SELECT id FROM `TB_TEACHER`) T1 where T1.id= ?";
    var stmt_duplicated2="INSERT INTO TB_PRINCIPAL (id,pwd,name,phoneNum,kinderCode) VALUES(?,?,?,?,?)";

    connection.query(stmt_duplicated,inputId,(err,rs)=>{

        if(rs[0]){
            status='0';
            res.json({status:status});
        }else{

            connection.query(stmt_duplicated2,[inputId,inputPwd,inputName,inputPhoneNum,kinderCode] ,(err,rs)=>{
            status='1';
            res.json({status:status});

            });
        }
    });



});
////<----------여기까지 회원등록 액션



////<--------------회원탈퇴 액션
app.post('/deletemember',function(req,res){

      const id = req.body.id;
      const inputPwd=req.body.inputPwd;
      const checkUserType=req.body.checkUserType;


      var stmt_duplicated="SELECT id FROM (SELECT id,pwd  FROM `TB_PARENTS` UNION SELECT id, pwd FROM `TB_PRINCIPAL` UNION SELECT id, pwd FROM `TB_TEACHER`) T1 where id= ? and pwd = ?";

      connection.query(stmt_duplicated, [id,inputPwd], function(err,rows) {

        if(!rows[0]){
            status='0';
            res.json({status:status});
        }else{

          if(checkUserType==0){ //교사
        var stmt_duplicated2="DELETE FROM TB_TEACHER  where id=? "
        connection.query(stmt_duplicated2, [id], function(err,rows) {


          if(!err){
            status='1';
            res.json({status:status});
          }
          else{
            status='0';
            res.json({status:status});
          }
        })
      }
      else if(checkUserType==1){ // 학부모

        var stmt_duplicated2="DELETE FROM TB_PARENTS  where id=? "
        connection.query(stmt_duplicated2, [id], function(err,rows) {

          if(!err){
              status='1';
              res.json({status:status});
          }else{

              status='0';
              res.json({status:status});


          }

        })


      }
      else if(checkUserType==2){ // 원장
        var stmt_duplicated2="DELETE FROM TB_PRINCIPAL  where id=? "
        connection.query(stmt_duplicated2, [id], function(err,rows) {


          if(!err){
            status='1';
            res.json({status:status});
          }
          else{
            status='0';
            res.json({status:status});
          }

        })
      }
        }

       })

});

app.post('/deletekids',function(req,res){
      console.log("학부모 id : ");
      const id = req.body.id;

      console.log("학부모 id : " + id);

      var stmt_duplicated = 'SELECT  * FROM TB_KIDS WHERE parentsId= ?';

      var stmt_duplicated2 = 'DELETE FROM TB_KIDS WHERE parentsId = ?';

      connection.query(stmt_duplicated, [id], function(err,rows) {

        if(err){
          status='0';
            res.json({status:status});
        }else{
          connection.query(stmt_duplicated2, [id], function(err,rows) {
            if(err){
              status='0';
              res.json({status:status});
            }
            else{
              status='1';
              res.json({status:status});
            }
        })
      }
      })



});
////////////<----------------------- 여기까지

/////////////<--------------------- 회원 수정
app.post('/updatemember',function(req,res){

      const id = req.body.id;
      const inputPwd = req.body.inputPwd;
      const inputName = req.body.inputName;
      const inputPhoneNum = req.body.inputPhoneNum;
      const  checkUserType= req.body.checkUserType;

      console.log("학부모 id : " + id);
      console.log("학부모 id : " + inputPwd);
      console.log("학부모 id : " + inputName);
      console.log("학부모 id : " + inputPhoneNum);
      console.log("학부모 id : " + checkUserType);

      if(checkUserType==0){ //교사
      var stmt_duplicated = 'UPDATE TB_TEACHER SET pwd = ?, name = ? , phoneNum = ? WHERE id = ?';
      connection.query(stmt_duplicated, [inputPwd,inputName,inputPhoneNum,id], function(err,rows) {
      if(err){
        status='0';
        res.json({status:status});
      }
      else{
        status='1';
        res.json({status:status});
      }
      })
      }
      else if(checkUserType==1){ // 학부모

      var stmt_duplicated = 'UPDATE TB_PARENTS SET pwd = ?, name = ? , phoneNum = ? WHERE id = ?';
      connection.query(stmt_duplicated, [inputPwd,inputName,inputPhoneNum,id], function(err,rows) {
        if(err){
          status='0';
          res.json({status:status});
        }
        else{
          status='1';
          res.json({status:status});
        }


      })


      }
      else if(checkUserType==2){ // 원장
      var stmt_duplicated = 'UPDATE TB_PRINCIPAL SET pwd = ?, name = ? , phoneNum = ? WHERE id = ?';
      connection.query(stmt_duplicated,[inputPwd,inputName,inputPhoneNum,id], function(err,rows) {


      if(err){
        status='0';
        res.json({status:status});
      }
      else{
        status='1';
        res.json({status:status});
      }

      })
      }



});

app.get('/getKinderList', function(req,res){
  //var kinderCode = req.body.kinderCode;
  console.log('getKinderList');
    console.log('getKinderList');
      console.log('getKinderList');


  var stmt_duplicated = "SELECT * from TB_kinder";

  connection.query(stmt_duplicated, function(err,rows) {
    var status = rows.length; // 자식 명수가 저장됨, 없으면 무조건 0
    if(err){
      console.log(err);
      return done(err);
    }else{
      console.log(rows);
      console.log(status);
      res.json({status:status, rows:rows});

    }
  })
})

app.post('/insertKinder', (req, res) => {

  var kinderCode = req.body.kinderCode;
  var kinderName = req.body.kinderName;
  var address = req.body.address;
  var phoneNum = req.body.PhoneNum;
  var id = req.body.id;

  console.log('kinderCode : ' + kinderCode);
  console.log('name : ' + kinderName);
  console.log('address : ' + address);
  console.log('phoneNum : ' + phoneNum );

  var stmt_duplicated = 'UPDATE TB_TEACHER SET pwd = ?, name = ? , phoneNum = ? WHERE id = ?';

  var statement = 'INSERT INTO TB_kinder (kinderCode, kinderName, address, PhoneNum) VALUES (?,?,?,?)';
  var statement2 = 'UPDATE TB_PRINCIPAL SET kinderCode = ? WHERE id = ?';
  var statement3 = 'INSERT INTO TB_CLASS (kinderCode, classCode, className) VALUES(?,?,?)'
  var params = [kinderCode, kinderName, address, phoneNum];
  connection.query(statement, params, (err, rows) => {
    if(err) {

      res.json({status:'0'});
    } else {
      connection.query(statement2, [kinderCode,id], (err, rows) => {
        if(err){
          res.json({status:'0'});
        }
        else{
          connection.query(statement3, [kinderCode,"0","없음"], (err, rows) => {
            if(err){
              res.json({status:'0'});
            }
            else{
              res.json({status:'1'});
            }
          })
        }
      })

    }
  })
});

//<------ 새 게시물 작성 ------>//
app.post('/newPost', function(req, res){
	console.log("***************************** newPost *****************************");
  var userId = req.body.userId;
  var name = req.body.name;
  var title = req.body.title;
  var texts = req.body.texts;
  var date = req.body.date;
  var kinderCode = req.body.kinderCode;
  var classCode = req.body.classCode;
  var files = req.body.files;

  console.log("게시자 id : " + userId);
  console.log("title : " + title);
  console.log("texts : " + texts);
  console.log("date : " + date);
  console.log("name : " + name);
  //console.log("fileData : " + fileData);

  var stmt_duplicated = 'INSERT INTO `TB_POST` (userId, name, title, texts, date, kinderCode, classCode)'
   + ' VALUES (?, ?, ?, ?, ?, ?, ?)'

  connection.query(stmt_duplicated, [userId, name, title, texts, date, kinderCode, classCode], function(err, rows) {

    if(err) {
      res.json(err);
    } else {

      // 파일이 업로드 되어있다면
      if (files) {

        var stmt_getNewPost = 'SELECT id FROM `TB_POST`'
         + ' WHERE TB_POST.userId = ? ORDER BY date DESC limit 1'
         connection.query(stmt_getNewPost, [userId], function(err2, rows2) {

           var postId = rows2[0].id;

           for (var i in files) {

             var fileName = files[i].fileName;
             var fileData = files[i].fileData;
             console.log("jsonObj : " + files[i]);
             console.log("fileName : " + fileName);
             console.log("fileData : " + fileData);

             var stmt_addFile = 'INSERT INTO `TB_POST_FILES` (postId, fileName, fileData)'
              + ' VALUES(?, ?, ?)'

              console.log("postId : " + postId);
              connection.query(stmt_addFile, [postId, fileName, fileData], function(err3, rows3) {

                res.json({status:0});  // 안드로이드로 status = 0을 보냄
              })
           }
         })
      }
    }
  })
})
//<------ 여기까지 ------>//

//<------ 게시물 업데이트 ------>//
app.post('/updatePost', function(req, res){
	console.log("***************************** updatePost *****************************");
  var id = req.body.id;
  var title = req.body.title;
  var texts = req.body.texts;
  var date = req.body.date;
  var files = req.body.files;

  var stmt_duplicated = 'UPDATE `TB_POST`'
   + ' SET title = ?, texts = ?, date = ?'
   + ' WHERE TB_POST.id = ?'

  connection.query(stmt_duplicated, [title, texts, date, id], function(err,rows) {

    if(err) {
      res.json(err);
    } else {

      // 업데이트가 성공하면, 연결된 파일도 모두 업데이트 한다.
      // 업데이트 로직은 postId 로 찾아서 삭제한 후, 다시 생성.
      var stmt_deleteFile = 'DELETE FROM `TB_POST_FILES` WHERE postId = ?'
      connection.query(stmt_deleteFile, [id], function(err2,rows2) {

        if (files) {

          for (var i in files) {

            var fileName = files[i].fileName;
            var fileData = files[i].fileData;
            console.log("jsonObj : " + files[i]);
            console.log("fileName : " + fileName);
            console.log("fileData : " + fileData);

            var stmt_addFile = 'INSERT INTO `TB_POST_FILES` (postId, fileName, fileData)'
             + ' VALUES(?, ?, ?)'

             console.log("postId : " + id);
             connection.query(stmt_addFile, [id, fileName, fileData], function(err3, rows3) {

               res.json({status:0});  // 안드로이드로 status = 0을 보냄
             })
          }
        }
      })
    }
  })
})
//<------ 여기까지 ------>//

//<------ 게시물 삭제 ------>//
app.post('/deletePost', function(req, res){
	console.log("***************************** deletePost *****************************");
  var id = req.body.id;

  var stmt_duplicated = 'DELETE FROM `TB_POST`'
   + ' WHERE TB_POST.id = ?'

  connection.query(stmt_duplicated, [id], function(err,rows) {

    if(err) {
      res.json(err);
    } else {

      var stmt_deleteFile = 'DELETE FROM `TB_POST_FILES` WHERE postId = ?'
      connection.query(stmt_deleteFile, [id], function(err2,rows2) {

        res.json({status:0});  // 안드로이드로 status = 0을 보냄
      })
    }
  })
})
//<------ 여기까지 ------>//

//<------ kinderCode 및 classCode 로 게시물 정보 리스트를 가져오는 라우터 ------>//
app.post('/getPostList', function(req, res){
	console.log("***************************** getPostList *****************************");
  var kinderCode = req.body.kinderCode;
  var classCode = req.body.classCode;
  var userCode = req.body.userCode;
  console.log("kinderCode : " + kinderCode);
  console.log("classCode : " + classCode);
  console.log("userCode : " + userCode);

  var stmt_duplicated = 'SELECT * FROM (SELECT tb_post.id, userId, TB_POST.name as userName, TB_POST.kinderCode, TB_POST.classCode, title, texts, date, hitCount from `TB_POST`'
	   + ' LEFT JOIN tb_teacher ON tb_post.userId = tb_teacher.id'
     + ' LEFT JOIN tb_principal ON tb_post.userId = tb_principal.id'
	   + ' where TB_POST.kinderCode = ? AND TB_POST.classCode = ? ORDER BY date DESC) AS SUBRESULT WHERE userName IS NOT NULL';


	  connection.query(stmt_duplicated, [kinderCode, classCode], function(err, rows) {

		if(err) {
		  res.json(err);
		} else {
		  //console.log(rows);
		  //console.log(rows.length);cd
		  var status = rows.length;

		  if(status == 0) {
			     //console.log(status);
			   res.json({status:status});  // 안드로이드로 status = 0을 보냄
		  } else {

        res.json({status:status, result:rows});
		  }
		}
  })
})
//<------ 여기까지 ------>//

//<------ 해당 post 에 첨부된 파일을 가져오는 라우터 ------>//
app.post('/getPostAttachFile', function(req, res){
	console.log("***************************** getPostAttachFile *****************************");
  var postId = req.body.postId;
  console.log("postId : " + postId);

  var stmt_duplicated = 'SELECT tb_post_files.id, postId, fileName, fileData from `TB_POST_FILES`'
   + ' where `postId` = ?';

  connection.query(stmt_duplicated, [postId], function(err,rows) {

    if(err) {
      res.json(err);
    } else {
      //console.log(rows);
      //console.log(rows.length);
      var status = rows.length;

      if(status == 0) {
        //console.log(status);
        res.json({status:status});  // 안드로이드로 status = 0을 보냄
      } else {
        res.json({status:status, result:rows});
      }
    }
  })
})
//<------ 여기까지 ------>//

//<------ 교사 및 원장 id를 받아서 게시물 정보를 가져오는 라우터 ------>//
app.post('/getPost', function(req, res){
	console.log("***************************** getPost *****************************");
  var id = req.body.id;

  var stmt_duplicated = 'SELECT tb_post.id, userId, tb_teacher.name as userName, title, texts, date, hitCount from `TB_POST`'
   + ' LEFT JOIN tb_teacher ON tb_post.userId = tb_teacher.id'
   + ' where `tb_post.id` = ?';
   + ' UNION'
   + 'SELECT tb_post.id, userId, tb_principal.name as userName, title, texts, date, hitCount from `TB_POST`'
   + ' LEFT JOIN tb_principal ON tb_post.userId = tb_principal.id'
   + ' where `tb_post.id` = ?';

  connection.query(stmt_duplicated, [id, id], function(err,rows) {

    if(err) {
      res.json(err);
    } else {
      //console.log(rows);
      //console.log(rows.length);
      var status = rows.length;

      if(status == 0) {
        //console.log(status);
        res.json({status:status});  // 안드로이드로 status = 0을 보냄
      } else {
        res.json({rows:rows[0]});
      }
    }
  })
})
//<------ 여기까지 ------>//

//<------ postId 로 게시물 정보 hitCount 를 증가시키는 라우터 ------>//
app.post('/countPostHitCount', function(req, res){
	console.log("***************************** countPostHitCount *****************************");
  var postId = req.body.postId;
  console.log("postId : " + postId);

  var stmt_duplicated = 'UPDATE TB_POST SET hitCount = hitCount + 1 WHERE id = ?'

  connection.query(stmt_duplicated, [postId], function(err,rows) {

    if(err) {
      res.json(err);
    } else {
      res.json({status:0});  // 안드로이드로 status = 0을 보냄
    }
  })
})
//<------ 여기까지 ------>//

//<------ class 리스트를 가져오는 라우터 ------>//
app.post('/getClassList', function(req, res){
	console.log("***************************** getClassList *****************************");

  var stmt_duplicated = 'SELECT * from `TB_CLASS` where classCode != 0 ORDER BY kinderCode, classCode'

  connection.query(stmt_duplicated, function(err,rows) {

    if(err) {
      res.json(err);
    } else {
      console.log(rows);
      //console.log(rows.length);
      var status = rows.length;

      if(status == 0) {
        //console.log(status);
        res.json({status:status});  // 안드로이드로 status = 0을 보냄
      } else {
        res.json({status:status, result:rows});
      }
    }
  })
})
//<------ 여기까지 ------>//

app.get('/getTest', function(req, res){
  res.send("ㅎㅇ");
})

app.listen(3000, function() {
   console.log('Connected 3000 port!');
});

// Delete Kid
app.post('/deleteKidByKidInfo', function(req,res){
  console.log('deleteKidByKidInfo');
  var Class = req.body.Class;
  var KidName = req.body.KidName;

  stmt_duplicated = 'DELETE FROM tb_Kids WHERE classCode = ? and name = ?';

  connection.query(stmt_duplicated, [Class,KidName], function(err,rows){
    var Msg = 'Success';
    console.log(stmt_duplicated);

    if(err) {
      return done(err);
    }
    res.json({Msg:Msg});
  })
})

// Update KidClass
app.post('/updateKidClass', function(req,res){
  console.log('updateKidClass');
  var KidName = req.body.name;
  var ClassCode = req.body.classCode;
  var stmt_duplicated = 'UPDATE tb_Kids SET classCode = ? WHERE name = ?';

  connection.query('SELECT * FROM tb_Kids WHERE name = ?', [KidName], function(err,rows,fields){
    if(!err){
      console.log(rows);
      connection.query(stmt_duplicated, [ClassCode , KidName], function(err,rows,fields){
        var Msg = 'Success';

        if(err){
          return done(err);
        }
        else{
          res.json({Msg:Msg});
        }
      })
    }
  })
})

// Update Kid
app.post('/updateKid', function(req,res){
  console.log('UpdateKid');
  var KidName = req.body.Name;
  var ParentsID = req.body.parentsID;
  var Birth = req.body.Birth;
  var Address = req.body.Address;
  var stmt_duplicated = 'UPDATE tb_Kids SET Birth = ? , address = ? WHERE name = ?';

  connection.query('SELECT * FROM tb_Kids WHERE parentsID = ? and name = ?', [ParentsID , KidName], function(err,rows,fields){
    if(!err){
      console.log(rows);
      connection.query(stmt_duplicated, [Birth, Address, KidName], function(err,rows,fields){
        var Msg = 'Success';

        if(err){
          return done(err);
        }
        else{
          res.json({Msg:Msg});
        }
      })
    }
  })
})

// Insert Kid
app.post('/insertKid', function(req,res)
{
  console.log('insertKid');
  console.log(req.body);

  var ParentsId = req.body.parentsId;
  var KidName = req.body.KidName;
  var Birth = req.body.Birth;
  var Address = req.body.Address;
  var KinderCode = req.body.KinderCode;
  var stmt_duplicated = 'INSERT INTO tb_Kids(parentsId, name, birth, address, kinderCode, classCode, state) VALUES (?, ?, ?, ?, ?, ?, ?);';

  connection.query(stmt_duplicated, [ParentsId,KidName,Birth,Address,KinderCode, "0", 0], function(err,rows){
    var Msg = 'Success';

    if(err) {
      return done(err);
    }
    res.json({Msg:Msg});
  })
})


// Display Kid
app.post('/displayKids', function(req,res)
{
  console.log('DisplayKids');
  var KinderName = req.body.KinderName;

  var stmt_duplicated = 'SELECT DISTINCT parentsId , birth , tb_kids.NAME, tb_kids.address, tb_kids.kinderCode, tb_kids.classCode  FROM tb_kids '
  + 'JOIN tb_kinder ON tb_kinder.KinderName = ? '
  + 'JOIN tb_teacher ON tb_teacher.KinderCode = tb_kinder.kinderCode '
  + 'WHERE tb_teacher.kinderCode = tb_kids.kinderCode and tb_kids.state = "1" '

  connection.query(stmt_duplicated, [KinderName] ,function(err,rows)
  {
    console.log(rows);
    console.log(rows.length);
    var status = rows.length;

    if(err)
    {
      return done(err);
    }
    else
    {
        if(status == 0)
        {
          res.json({status:status});
        }
        else
        {
          res.json({status:status, rows:rows});
        }
    }
  })
})

// Display Class
app.post('/displayClass', function(req,res)
{
  console.log('DisplayClass');
  var KinderName = req.body.KinderName;
  console.log(KinderName);

  var stmt_duplicated = 'SELECT tb_class.kinderCode , tb_class.classCode, tb_class.className FROM tb_class '
  + 'JOIN tb_kinder ON tb_kinder.KinderCode = tb_class.kinderCode AND tb_kinder.kinderName = ? ';

  connection.query(stmt_duplicated,[KinderName] ,function(err,rows)
  {
    console.log(rows);
    console.log(rows.length);
    var status = rows.length;

    if(err)
    {
      return done(err);
    }
    else
    {
        if(status == 0)
        {
          console.log(status);
          res.json({status:status});
        }
        else
        {
          for(var i = 0; i < status; i++)
          {
            console.log("유치원 : " + rows[i].kinderCode);
            console.log("반번호 : " + rows[i].classCode);
            console.log("반이름 : " + rows[i].className);
          }
          res.json({status:status, rows:rows});
        }
    }
  })
})


// Insert BoardingList
app.post('/insertBoardingList', function(req,res){
  console.log('insertBoardingList');

  var BusNo = req.body.BusNo;
  var KidName = req.body.KidName;
  var ParentsID= req.body.parentsID;
  var KinderCode = req.body.kinderCode;

  var stmt_duplicated = 'INSERT INTO tb_boardinglist(BusNo, parentsID, name, kinderCode) VALUES (?, ?, ?, ?);';

  connection.query(stmt_duplicated, [BusNo,ParentsID,KidName,KinderCode], function(err,rows){
    var Msg = 'Success';

    if(err) {
      return done(err);
    }
    res.json({Msg:Msg});
  })
})

// Update BoardingList
app.post('/updateBoardingList', function(req,res){
  console.log('UpdateBoardingList');

  var BusNo = req.body.BusNo;
  var KidName = req.body.name;
  var stmt_duplicated = 'UPDATE tb_boardinglist SET BusNo = ? WHERE name = ?';

  connection.query('SELECT * FROM tb_boardinglist WHERE BusNo = ? and name = ?', [BusNo, KidName], function(err,rows,fields){
    if(!err){
      console.log(rows);
      connection.query(stmt_duplicated, [BusNo, KidName], function(err,rows,fields){
        var Msg = 'Success';

        if(err){
          return done(err);
        }
        else{
          res.json({Msg:Msg});
        }
      })
    }
  })
})

// Delete BoardingList
app.post('/deleteBoardingList', function(req,res){
  console.log('DeleteBoardingList');
  var stmt_duplicated = '';
  var Length = req.body.length;
  console.log(req.body);

  stmt_duplicated = 'DELETE FROM tb_boardinglist WHERE BusNo = "' + req.body.BusNo + '" and name = "' + req.body.KidName + '"; ';

  connection.query(stmt_duplicated, function(err,rows){
    var Msg = 'Success';
    console.log(stmt_duplicated);

    if(err) {
      return done(err);
    }
    res.json({Msg:Msg});
  })
})


// Display BusNo
app.post('/displayBusNo', function(req,res)
{
  console.log('DisplayBusNo');
  var WHO = req.body.WHO;
  var KinderName = req.body.KinderName;
  var stmt_duplicated;
  if(WHO == "Teacher"){
    stmt_duplicated = 'SELECT DISTINCT BusNo  FROM tb_bus '
    + 'JOIN tb_kinder ON tb_kinder.KinderName = ? '
    + 'JOIN tb_teacher ON tb_teacher.KinderCode = tb_kinder.kinderCode '
    + 'WHERE tb_teacher.kinderCode = tb_bus.kinderCode';
  }
  if(WHO == "Principal"){
    stmt_duplicated = 'SELECT DISTINCT BusNo  FROM tb_bus '
    + 'JOIN tb_kinder ON tb_kinder.KinderName = ? '
    + 'JOIN tb_principal ON tb_principal.KinderCode = tb_kinder.kinderCode '
    + 'WHERE tb_principal.kinderCode = tb_bus.kinderCode';
  }

  connection.query(stmt_duplicated,[KinderName],function(err,rows)
  {
    console.log(rows);
    console.log(rows.length);
    var status = rows.length;

    if(err)
    {
      return done(err);
    }
    else
    {
        if(status == 0)
        {
          console.log(status);
          res.json({status:status});
        }
        else
        {
          for(var i = 0; i < status; i++)
          {
            console.log("버스 : " + rows[i].BusNo);
          }
          res.json({status:status, rows:rows});
        }
    }
  })
})

// Display BoardingList
app.post('/displayBoardingList', function(req,res)
{
  console.log('DisplayBoardingList');
  var WHO = req.body.WHO;
  var KinderName = req.body.KinderName;
  var stmt_duplicated;
  if(WHO == "Teacher"){
    stmt_duplicated = 'SELECT DISTINCT BusNo, tb_boardinglist.name  FROM tb_boardinglist '
    + 'JOIN tb_kinder ON tb_kinder.KinderName = ? '
    + 'JOIN tb_teacher ON tb_teacher.KinderCode = tb_kinder.kinderCode '
    + 'WHERE tb_teacher.kinderCode = tb_boardinglist.kinderCode'
  }
  if(WHO == "Principal"){
    stmt_duplicated = 'SELECT DISTINCT BusNo, tb_boardinglist.name  FROM tb_boardinglist '
    + 'JOIN tb_kinder ON tb_kinder.KinderName = ? '
    + 'JOIN tb_principal ON tb_principal.KinderCode = tb_kinder.kinderCode '
    + 'WHERE tb_principal.kinderCode = tb_boardinglist.kinderCode'
  }

  connection.query(stmt_duplicated,[KinderName],function(err,rows)
  {
    console.log(rows);
    console.log(rows.length);
    var status = rows.length;

    if(err)
    {
      return done(err);
    }
    else
    {
        if(status == 0)
        {
          console.log(status);
          res.json({status:status});
        }
        else
        {
          for(var i = 0; i < status; i++)
          {
            console.log("버스 : " + rows[i].BusNo);
            console.log("원아 : " + rows[i].name);
          }
          res.json({status:status, rows:rows});
        }
    }
  })
})

// 유치원코드로 버스 정보 가져오기
app.get('/getBusList', function(req,res){
  var kinderCode = req.query.kinderCode;
  console.log('getBusList');
  console.log('kinderCode : ' + kinderCode);

  var stmt_duplicated = `SELECT BusNo, seat from TB_bus WHERE kinderCode=?`;

  connection.query(stmt_duplicated, kinderCode , function(err,rows) {
    if(err){
      console.log(err);
      return done(err);
    }else{
      console.log(rows);
      res.json({rows:rows});
    }
  })
})

app.get('/insertBus', (req, res) => {

  var busNo = req.query.busNo;
  var kinderCode = req.query.kinderCode;
  var seat = req.query.seat;

  console.log('busNo : ' + busNo);
  console.log('kinderCode : ' + kinderCode);
  console.log('seat : ' + seat);

  var statement = `INSERT INTO TB_bus (BusNo, kinderCode, Seat) VALUES (?,?,?)`;
  var params = [busNo, kinderCode, seat];
  connection.query(statement, params, (err, rows) => {
    if(!err) {
      res.json({'result':200});
    } else {
      res.json({'result':400});
    }
  })
});

app.get('/updateBus', (req, res) => {

  var kinderCode = req.query.kinderCode;
  var busNo = req.query.busNo;
  var seat = req.query.seat;
  console.log('busNo : ' + busNo);
  console.log('kinderCode : ' + kinderCode);
  console.log('seat : ' + seat);


  var statement = `UPDATE TB_bus SET Seat = ? WHERE BusNo = ? and kinderCode = ?`;
  var params = [seat, busNo, kinderCode];
  connection.query(statement, params, (err, rows) => {
    if(!err) {
      res.json({'result':200});
    } else {
      res.json({'result':400});
    }
  })
});

app.get('/deleteBus', (req, res) => {

  var busNo = req.query.busNo;
  console.log('busNo : ' + busNo);

  var delete_statement = `DELETE FROM TB_bus WHERE busNo=?`;

  connection.query(delete_statement, busNo, (err, rows) => {
    if(!err) {
      res.json({'result':200});
    } else {
      res.json({'result':400});
    }
  })
});


app.get('/getClassList2', function(req,res){
  var kinderCode = req.query.kinderCode;
  console.log('getClassList2');
  console.log('kinderCode : ' + kinderCode);
  //console.log('kinderCode : ' + kinderCode);

  var stmt_duplicated = `SELECT classCode, className from TB_class WHERE kinderCode=? and classCode != 0 order by classCode*1`;

  connection.query(stmt_duplicated, kinderCode , function(err,rows) {
    if(err){
      console.log(err);
      return done(err);
    }else{
      console.log(rows);
      res.json({rows:rows});
    }
  })
})

app.get('/insertClass', (req, res) => {

  var kinderCode = req.query.kinderCode;
  var classCode = req.query.classCode;
  var className = req.query.className;
  console.log('kinderCode : ' + kinderCode);
  console.log('classCode : ' + classCode);
  console.log('className : ' + className);

  var statement = `INSERT INTO TB_class (kinderCode, classCode, className) VALUES (?,?,?)`;
  var params = [kinderCode, classCode, className];
  connection.query(statement, params, (err, rows) => {
    if(!err) {
      res.json({'result':200});
    } else {
      res.json({'result':400});
    }
  })
});

  app.get('/deleteClass', (req, res) => {
  var kinderCode = req.query.kinderCode;
  var classCode = req.query.classCode
  console.log('kinderCode : ' + kinderCode);
  console.log('classCode : ' + classCode);

  var delete_statement = `DELETE FROM TB_class WHERE kinderCode=? and classCode=?`;
  var params = [kinderCode, classCode]
  connection.query(delete_statement, params, (err, rows) => {
    if(!err) {
      res.json({'result':200});
    } else {
      res.json({'result':400});
    }
  })
});
