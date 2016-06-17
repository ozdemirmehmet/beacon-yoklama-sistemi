
<?php
	require_once("connect.php");
	if( isset($_POST["studentId"]) & isset($_POST["major"]) & isset($_POST["minor"]) ){
		header('content-type:text/html;charset=utf-8');
		$studentId = $_POST["studentId"];
		$major = $_POST["major"];
		$minor = $_POST["minor"];

		$sql1 = "SELECT id,name FROM class WHERE major='$major' AND minor='$minor'";
		$result1 = $con->query($sql1);//Burada o gelen major ve minor değerlerine ait sınıf olup olmadığını kontrol edecek sorguyu çalıştırdık.
		if($result1->num_rows > 0){//Bu major ve minor değerlerine ait sınıf var ise
			$row1 = $result1->fetch_assoc();
			$classId = $row1['id'];
			$className = $row1['name'];

			//İçinde bulunulan saat ve günde o sınıfta hangi dersin olduğunu kontrol edecek sorgumuz
			$sql2 = "SELECT lesson.id FROM lesson INNER JOIN schedule ON lesson.scheduleId=schedule.id WHERE 
			CURRENT_TIME() BETWEEN schedule.startTime AND schedule.endTime AND schedule.day = DAYNAME(CURRENT_DATE()) AND lesson.classId = '$classId'";
			$result2 = $con->query($sql2);
			if($result2->num_rows > 0){//O anda o sınıfta bir ders var ise
				$row2 = $result2->fetch_assoc();
				$lessonId = $row2['id'];

				//Derse ait verilerin alındığı sorgumuz
				$sql = "SELECT lesson.lessonName,lesson.term,instructor.firstName,instructor.lastName,instructor.rank FROM lesson 
				INNER JOIN instructor ON lesson.instructorId=instructor.id WHERE lesson.id = '$lessonId'";
				$result = $con->query($sql);
				$row = $result->fetch_assoc();

				$lessonName = $row['lessonName'];
				$lessonTerm = $row['term'];
				$instructorFirstName = $row['firstName'];
				$instructorLastName = $row['lastName'];
				$instructorRank = $row['rank'];


				//İstekle bereaber gelen öğrenci id sinin ders kayıtları içerisinde o anda bulunduğu sınıftaki dersin olup olmadığını kontrol edecek sorgumuz
				$sql3 = "SELECT enrollment.id FROM enrollment INNER JOIN student ON enrollment.studentId = student.id WHERE 
				enrollment.lessonId = '$lessonId' AND enrollment.studentId = '$studentId'";
				$result3 = $con->query($sql3);
				if($result3->num_rows > 0){//Eğer o öğrenci bulunduğu sınıftaki dersi alıyorsa

					//Öğrencinin o tarihe ait yoklama kaydını kontrol edecek sorgumuz
					$sql7 = "SELECT id FROM attendance WHERE studentId='$studentId' AND dateAttented = CURRENT_DATE()";
					$result7 = $con->query($sql7);
					if($result7->num_rows > 0){//Öğrencinin o tarihe ait yoklama kaydı bulunuyor
						$row7 = $result7->fetch_assoc();
						$id = $row7['id'];

						//Öğrencinin bulunulan tarih ve saat içinde yoklamaya olan veritabanı kaydını kontrol edecek sorgumuz
						$sql5 = "SELECT attendance_hours.hour FROM attendance_hours WHERE attendance_hours.attendanceId = '$id' AND attendance_hours.hour = concat(hour(now()),':00:00')";
						$result5 = $con->query($sql5);
						if($result5->num_rows > 0){//Öğrencinin bu saat için ders kaydı daha önceden yoklamaya yapılmış
							$response = array(
								"response" => "ALREADY_SIGNED"
								);
						}
						else{//Bu saat için yoklama kaydı bulunmuyor
							$sql6 = "INSERT INTO attendance_hours (attendanceId, hour) VALUES ('$id',concat(hour(now()),':00:00'));";
							if(!mysqli_query($con, $sql6)){//kayıt gerçekleşmez ise
  								$response = array(
									"response" => "ERROR_SIGN"
									);	
							}
							else{
								$response = array(
									"response" => "OK_SIGN",
									"lesson" => array(
										"name" => $lessonName,
										"term" => $lessonTerm,
										"className" => $className,
										"instructor" => array(
											"firstName" => $instructorFirstName,
											"lastName" => $instructorLastName,
											"rank" => $instructorRank
											)
										)
									);	
							}
						}
					}
					else{//Öğrencinin o tarihe ait yoklama kaydı bulunmuyor
						$sql4 = "INSERT INTO attendance (studentId, dateAttented, lessonId) VALUES ('$studentId',CURRENT_DATE(),'$lessonId')";
						if(!mysqli_query($con, $sql4)){//kayıt gerçekleşmez ise
  							$response = array(
								"response" => "ERROR_SIGN"
								);	
						}
						else{//Katılım kaydı başarılı
							$id = $con->insert_id;

							$sql6 = "INSERT INTO attendance_hours (attendanceId, hour) VALUES ('$id',concat(hour(now()),':00:00'));";
							if(!mysqli_query($con, $sql6)){//kayıt gerçekleşmez ise
  								$response = array(
									"response" => "ERROR_SIGN"
									);	
							}
							else{
								$response = array(
									"response" => "OK_SIGN",
									"lesson" => array(
										"name" => $lessonName,
										"term" => $lessonTerm,
										"className" => $className,
										"instructor" => array(
											"firstName" => $instructorFirstName,
											"lastName" => $instructorLastName,
											"rank" => $instructorRank
											)
										)
									);	
							}
						}
					}
				}
				else{//Öğrenci bulunduğu sınıftaki dersi almıyorsa
					$response = array(
						"response" => "NO_REGISTERED_LESSON"
						);
				}
			}
			else{//O anda o sınıfta ders yok ise
				$response = array(
					"response" => "NO_LESSON"
					);
			}
		}
		else{//Gelen major ve minor değerlerine ait sınıf yok ise
			$response = array(
				"response" => "NO_CLASS"
				);
		}

		echo json_encode($response,JSON_UNESCAPED_UNICODE);
		mysqli_close($con);
		
	}
?>