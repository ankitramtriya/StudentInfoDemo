package http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.multipart.FormDataParam;

import dao.Login;
import dao.Message;
import dao.MessageDao;
import dao.Student;
import dao.StudentDao;

@Path("/student")
public class Welcome {
	public static String toUser = "admin";
	
	@POST
	@Path("/signup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(Login login) {
		StudentDao studentDao = new StudentDao();
		if (studentDao.createLoginUser(login) == 1) {
			return Response.status(200).entity("success").build();
		} else {
			return Response.status(500).entity("failure").build();
		}
	}
	
	@POST
	@Path("/login")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response login(@FormDataParam("user")String user,
			@FormDataParam("password")String password) {
		toUser = user;
		StudentDao studentDao = new StudentDao();
		Login login = studentDao.getLoginUser(user, password);
		if(login == null){
			return Response.serverError().build();
		}else {
			return Response.status(200).entity("success").build();
		}
	}
	
	
	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addStudent(Student std) {
		StudentDao studentDao = new StudentDao();
		if (studentDao.createStudent(std) == 1) {
			return Response.status(200).entity("success").build();
		} else {
			return Response.status(500).entity("failure").build();
		}
	}
	
	@POST
	@Path("/searchSingle")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchSingleStudent(String rollNo) {
		StudentDao studentDao = new StudentDao();
		Student student = studentDao.getStudent(Integer.parseInt(rollNo));
		if(student == null){
			return Response.serverError().build();
		}else {
			int total = student.getChemistryMarks()+student.getPhysicsMarks()+student.getMathMarks();
			total=total/3;
			if(total >= 85) {
				student.setGrade("A");
			}else if(total <85 && total>=70) {
				student.setGrade("B");
			}else if(total <70 && total>=50) {
				student.setGrade("C");
			}else if(total <50 && total>=35) {
				student.setGrade("D");
			}else {
				student.setGrade("F");
			}
			return Response.status(200).entity(student).build();
		}
	}

	@POST
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchStudent(String rollNo) {
		StudentDao studentDao = new StudentDao();
		String[] rollNums = rollNo.replaceAll("[\"{}]", "").split(",");
		int fromRollNum = Integer.parseInt(rollNums[0].split(":")[1]);
		int toRollNum = Integer.parseInt(rollNums[1].split(":")[1]);
		List<Student> studentlist = studentDao.getMultipleStudent(fromRollNum, toRollNum);
		for (int i = 0; i < studentlist.size(); i++) {
			// System.out.println(temp.getRollNumber()+" "+temp.getName()+"
			// "+temp.getPhysicsMarks());
			studentlist.set(i, CalGrade(studentlist.get(i)));
		}
		if(!studentlist.isEmpty()) {
			return Response.status(200).entity(studentlist).build();
		}else {
			return Response.status(500).entity("failure").build();
		}
	}

	public Student CalGrade(Student st) {
		int total = st.getChemistryMarks() + st.getPhysicsMarks() + st.getMathMarks();
		st.setTotal();
		total = total / 3;
		if (total >= 85) {
			st.setGrade("A");
		} else if (total < 85 && total >= 70) {
			st.setGrade("B");
		} else if (total < 70 && total >= 50) {
			st.setGrade("C");
		} else if (total < 50 && total >= 35) {
			st.setGrade("D");
		} else {
			st.setGrade("F");
		}
		return st;
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadImage(@FormDataParam("pic") InputStream in, @FormDataParam("pic_url") String path)
			throws IOException {
		OutputStream outputStream = null;
		File file = new File(path + ".png");
		if (file.exists()) {
			file.delete();
		}
		try {
			outputStream = new FileOutputStream(new File(path + ".png"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return Response.status(500).entity("failure").build();
		}
		int read = 0;
		byte[] bytes = new byte[1024];

		try {
			while ((read = in.read(bytes)) != -1) {
				try {
					outputStream.write(bytes, 0, read);
				} catch (IOException e) {
					e.printStackTrace();
					return Response.status(500).entity("failure").build();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(500).entity("failure").build();
		} finally {
			outputStream.close();
		}
		return Response.status(200).entity("success").build();
	}

	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateStudent(Student std) {
		StudentDao studentDao = new StudentDao();
		if (studentDao.updateStudent(std) == 1) {
			return Response.status(200).entity("success").build();
		} else {
			return Response.status(500).entity("failure").build();
		}
	}
	
	@POST
	@Path("/sendmessage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendMessage(@FormDataParam("message")String message,
			@FormDataParam("toUser")String to_user) {
		Message msgObj = new Message();
		msgObj.setFromUser(toUser);
		msgObj.setMessage(message);
		msgObj.setToUser(to_user);
		TimeZone tz = TimeZone.getTimeZone("GMT");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
		df.setTimeZone(tz);
		String nowAsISO = df.format(new Date(System.currentTimeMillis()));
		msgObj.setSentOn(nowAsISO);
		MessageDao messageDao = new MessageDao();
		if(messageDao.insertMessage(msgObj) == 1) {
			return Response.status(200).entity("success").build();
		}else {
			return Response.status(500).entity("failure").build();
		}
	}
	
	@POST
	@Path("/getmessages")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMessages() {
		MessageDao messageDao = new MessageDao();
		List<Message> messageList = messageDao.getMessages(toUser);
		if(!messageList.isEmpty()) {
			return Response.status(200).entity(messageList).build();
		}else {
			return Response.status(500).entity("failure").build();
		}
	}
	
	@POST
	@Path("/deletemessage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response deleteMessage(@FormDataParam("id") int messageId) {
		MessageDao messageDao = new MessageDao();
		if(messageDao.deleteMessage(messageId) == 1) {
			return Response.status(200).entity("success").build();
		}else {
			return Response.status(500).entity("failure").build();
		}
	}
	
	@POST
	@Path("/getloginusers")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoginUsers(){
		StudentDao studentDao = new StudentDao();
		List<Login> allUsers = studentDao.getAllUsers(toUser);
		if(!allUsers.isEmpty()) {
			return Response.status(200).entity(allUsers).build();
		}else {
			return Response.status(500).entity("failure").build();
		}
	}
}
