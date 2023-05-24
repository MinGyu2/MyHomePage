~/webServer/apache-tomcat-10.1.7/bin/shutdown.sh
echo "톰캣 종료"
sleep 0.5
rm -r bin/classes/*
echo "삭제 완료"
sleep 0.3
javac -cp "lib/*" -sourcepath src/main/java/ -d bin/classes/ ./src/main/java/com/mingyu2/login/Login.java
echo "컴파일 완료1"
javac -cp "lib/*" -sourcepath src/main/java/ -d bin/classes/ ./src/main/java/com/mingyu2/happyhackingmain/MainPage.java
echo "컴파일 완료2"
javac -cp "lib/*" -sourcepath src/main/java/ -d bin/classes/ ./src/main/java/com/mingyu2/login/Logout.java
echo "컴파일 완료3"
sleep 0.3
cp -r ./bin/classes/* ~/webServer/myWeb/ROOT/WEB-INF/classes/
echo "복사 완료"
sleep 0.3
~/webServer/apache-tomcat-10.1.7/bin/startup.sh
echo "톰캣 시작"
sleep 1
nmap localhost