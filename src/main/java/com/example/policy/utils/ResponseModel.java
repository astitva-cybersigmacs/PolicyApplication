package com.example.policy.utils;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


public class ResponseModel {

	/**
	 * {@code Success Response}.
	 * 
	 * @author Mayank Jyoti Verma
	 * @param message must not be null
	 * @return <b>status</b>: 200- OK, <b>data</b>, <b>data count</b>
	 */
	public static ResponseEntity<Object> success(String message) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	public static ResponseEntity<Object> success(String message, Vector<Object> payload) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		data.put("data", payload);
		data.put("count", payload.size());
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	public static ResponseEntity<Object> success(String message, Optional<Object> payload) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		data.put("data", payload);
		data.put("count", 1);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	public static ResponseEntity<Object> success(String message, List<?> payload) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		data.put("data", payload);
		data.put("count", payload.size());
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	public static ResponseEntity<Object> success(String message, Object payload) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		data.put("data", payload);
		data.put("count", 1);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	public static ResponseEntity<Object> success(String message, Page<?> payload) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		data.put("data", payload.getContent());
		data.put("pageNumber", payload.getNumber());
		data.put("pageSize", payload.getSize());
		data.put("totalPage", payload.getTotalPages());
		data.put("totalElement", payload.getTotalElements());
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	public static ResponseEntity<Object> success(String message,List<?> payloadData, Page<?> payload) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		data.put("data", payloadData);
		data.put("pageNumber", payload.getNumber());
		data.put("pageSize", payload.getSize());
		data.put("totalPage", payload.getTotalPages());
		data.put("totalElement", payload.getTotalElements());
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	/**
	 * {@code Success Response with map payload}.
	 * 
	 * @author Mayank Jyoti Verma
	 * @param message must not be null
	 * @param payload Map<String, Object>
	 * @return <b>status</b>: 200- OK, <b>data</b>, <b>data count</b>
	 */
	public static ResponseEntity<Object> successMap(String message, Map<String, Object> payload) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		data.put("data", payload);
		data.put("count", payload.size());
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	public static ResponseEntity<Object> successMapObject(String message, Map<?, ?> payload) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		data.put("data", payload);
		data.put("count", payload.size());
		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	public static ResponseEntity<Object> update(String message) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	/**
	 * {@code Created Response}.
	 * 
	 * @author Mayank Jyoti Verma
	 * @param message must not be null
	 * @return <b>status</b>: 201- Created, <b>data</b>, <b>data count</b>
	 */
	public static ResponseEntity<Object> created(String message) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		return new ResponseEntity<>(data, HttpStatus.CREATED);
	}

	/**
	 * Return response 204 - Request accepted and haven't message.
	 * 
	 * @param message activate or deactivated
	 * @return 201 status code
	 */
	public static ResponseEntity<Object> activate_deactivate(String message) {
//		Map<String, Object> data = new HashMap<>();
//		data.put("status", "success");
//		data.put("message", message);
		return new ResponseEntity<>(message, HttpStatus.valueOf(204));
	}

	/**
	 * {@code Created Response}.
	 * 
	 * @author Mayank Jyoti Verma
	 * @param message must not be null
	 * @param payload Vector<Object>
	 * @return <b>status</b>: 201- Created, <b>data</b>, <b>data count</b>
	 */
	public static ResponseEntity<Object> createdWithPayload(String message, Vector<Object> payload) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		data.put("data", payload);
		return new ResponseEntity<>(data, HttpStatus.CREATED);
	}

	/**
	 * Custom validation error
	 * 
	 * @param fieldName    : field name which is not valid
	 * @param errorMessage : error message for validation
	 * @return Return custom error Response Data
	 */
	public static ResponseEntity<Object> customValidations(String fieldName, String errorMessage) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "fail");
		data.put("fieldName", fieldName);
		data.put("message", errorMessage);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	/**
	 * Response error
	 * 
	 * @author Mayank Jyoti Verma
	 * @return ResponseEntity<Object>(error message, status);
	 */
	public static ResponseEntity<Object> unknownError() {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "fail");
		data.put("message", "Something went wrong");
		return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
	}
	/**
	 * Response error
	 *
	 * @author Mayank Jyoti Verma
	 * @return ResponseEntity<Object>(error message, status);
	 */
	public static ResponseEntity<Object> unknownError(Exception e) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "fail");
		data.put("message", "Something went wrong");
		return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
	}
	/**
	 * Response error
	 *
	 * @author Mayank Jyoti Verma
	 * @return ResponseEntity<Object>(error message, status);
	 */
	public static ResponseEntity<Object> notFound(String message) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "fail");
		data.put("message", message);
		return new ResponseEntity<>(data, HttpStatus.NOT_FOUND);
	}

	/**
	 * Response error
	 * 
	 * @author Mayank Jyoti Verma
	 * @return ResponseEntity<Object>(error message, status);
	 */
	public static ResponseEntity<Object> error(String message) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "fail");
		data.put("message", message);
		return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Response Ok
	 * 
	 * @author Mayank Jyoti Verma
	 * @return ResponseEntity<Object>(error message);
	 */
	public static ResponseEntity<Object> oK(String message) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	public static ResponseEntity<Object> fileUploaded(String fileName, String fileType) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", "File uploaded successfully");
		data.put("fileName", fileName);
		data.put("fileType", fileType);

		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	public static ResponseEntity<Object> unsupportedMediaType() {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", "Unsupported Media Type");
		return new ResponseEntity<>(data, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	/**
	 * Send Media in Original format
	 * @author mayankjyotiverma
	 * @param fileData - original file data in byte
	 * @param fileType- type of the file like jpeg, jpg, pdf etc
	 * @return file as original format
	 */
	public static ResponseEntity<Object> sendMediaWithDecompress(byte[] fileData, String fileType) {
		// TODO Auto-generated method stub
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf(fileType)).body(FileUtils.decompressFile(fileData));
	}

	public static ResponseEntity<Object> deleted() {
		// TODO Auto-generated method stub
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
	

	/**
	 * mediaFile - Return file in Original format
	 * @param fileType
	 * @param report
	 * @return Original file
	 */
	public static ResponseEntity<Object> mediaFile(String fileType, byte[] report) {
		// TODO Auto-generated method stub
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf(fileType)).body(report);
	}

	/**
	 * Already Existed
	 * @param message - Data Already existed
	 * @return 208 status code
	 */
	public static ResponseEntity<Object> alreadyReported(String message) {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "success");
		data.put("message", message);
		return new ResponseEntity<>(data, HttpStatus.ALREADY_REPORTED);
	}
}

//1×× Informational
//100 Continue
//101 Switching Protocols
//102 Processing
//103 Early Hints

//2×× Success
//200 OK
//201 Created
//202 Accepted
//203 Non-authoritative Information
//204 No Content
//205 Reset Content
//206 Partial Content
//207 Multi-Status
//208 Already Reported
//226 IM Used

//3×× Redirection
//300 Multiple Choices
//301 Moved Permanently
//302 Found
//303 See Other
//304 Not Modified
//305 Use Proxy
//307 Temporary Redirect
//308 Permanent Redirect

//4×× Client Error
//400 Bad Request
//401 Unauthorized
//402 Payment Required
//403 Forbidden
//404 Not Found
//405 Method Not Allowed
//406 Not Acceptable
//407 Proxy Authentication Required
//408 Request Timeout
//409 Conflict
//410 Gone
//411 Length Required
//412 Precondition Failed
//413 Payload Too Large
//414 Request-URI Too Long
//415 Unsupported Media Type
//416 Requested Range Not Satisfiable
//417 Expectation Failed
//418 I'm a teapot
//421 Misdirected Request
//422 Unprocessable Entity
//423 Locked
//424 Failed Dependency
//425 Too Early
//426 Upgrade Required
//428 Precondition Required
//429 Too Many Requests
//431 Request Header Fields Too Large
//444 Connection Closed Without Response
//451 Unavailable For Legal Reasons
//499 Client Closed Request

//5×× Server Error
//500 Internal Server Error
//501 Not Implemented
//502 Bad Gateway
//503 Service Unavailable
//504 Gateway Timeout
//505 HTTP Version Not Supported
//506 Variant Also Negotiates
//507 Insufficient Storage
//508 Loop Detected
//510 Not Extended
//511 Network Authentication Required
//599 Network Connect Timeout Error
