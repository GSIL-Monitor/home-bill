package com.hb.web.util;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.S3ClientOptions.Builder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class AmazonS3 {

	private static Logger logger = LoggerFactory.getLogger(AmazonS3.class);

	private static String accessKey = PropertyUtil.get("amazons3.accessKey", "amazons3");
	private static String secretKey = PropertyUtil.get("amazons3.secretKey", "amazons3");

	private static String bucketName = PropertyUtil.get("amazons3.bucketName", "amazons3");
	private static boolean isPublicRead = Boolean.valueOf(PropertyUtil.get("amazons3.isPublicRead", "amazons3"));

	/** 是否使用 虚拟主机 true: host/bucketname false: bucketname.host 默认false*/
	private static boolean validDNS = Boolean.valueOf(PropertyUtil.get("amazons3.validDNS", "amazons3"));
	private static boolean protocolHttp = Boolean.valueOf(PropertyUtil.get("amazons3.protocolHttp", "amazons3"));
	private static String endpoint = PropertyUtil.get("amazons3.endpoint", "amazons3");

	public static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	public static byte[] copyToByteArray(InputStream in) throws IOException {
		int BUFFER_SIZE = 4096;
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = -1;
		while ((bytesRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}
		out.flush();
		return out.toByteArray();
	}

	private static String getDefaultBucketName() {
		return bucketName;
	}

	private static boolean getDefaultIsPublicRead() {
		return isPublicRead;
	}

	private static AmazonS3Client getAmazonS3() {
		AmazonS3Client conn = null;
		try {
			if (!isBlank(accessKey) && !isBlank(secretKey) && !isBlank(endpoint)) {
				AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
				ClientConfiguration clientConfig = new ClientConfiguration();
				clientConfig.setMaxConnections(300);
				clientConfig.setConnectionTTL(1000);
				if (protocolHttp) {
					clientConfig.setProtocol(Protocol.HTTP);
				} else {
					clientConfig.setProtocol(Protocol.HTTPS);
				}
				conn = new AmazonS3Client(credentials, clientConfig);

				Builder builder = S3ClientOptions.builder();
				if (validDNS) {
					builder.setPathStyleAccess(true);
				} else {
					builder.setPathStyleAccess(false);
				}
				S3ClientOptions clientOptions = builder.build();
				conn.setS3ClientOptions(clientOptions);

				Region region = Region.getRegion(Regions.CN_NORTH_1);
				conn.setRegion(region);

				conn.setEndpoint(endpoint);

			}
		} catch (Exception e) {
			throw new AmazonClientException(e.getMessage(), e);
		}
		if (conn != null) {
			return conn;
		} else {
			throw new AmazonClientException("Can not create AmazonS3Client !");
		}
	}

	/**
	 * 一旦关闭就不能再使用
	 * @param s3
	 */
	private static void closeAmazonS3Client(AmazonS3Client s3) {
		if (s3 != null) {
			s3.shutdown();
		}
	}

	public static AmazonS3Response upload(File file) throws AmazonClientException {
		String fileName = createFilename(file);
		String bucketName = getDefaultBucketName();
		boolean isPublicRead = getDefaultIsPublicRead();
		return upload(bucketName, fileName, file, isPublicRead);
	}

	public static AmazonS3Response upload(File file, String bucketName) throws AmazonClientException {
		return upload(bucketName, file);
	}

	public static AmazonS3Response upload(String bucketName, File file) throws AmazonClientException {
		boolean isPublicRead = getDefaultIsPublicRead();
		return upload(bucketName, file, isPublicRead);
	}

	public static AmazonS3Response upload(String bucketName, File file, boolean isPublicRead) throws AmazonClientException {
		return upload(bucketName, "", file, isPublicRead);
	}

	public static AmazonS3Response upload(String bucketName, String path, File file, boolean isPublicRead) throws AmazonClientException {
		AmazonS3Client s3 = getAmazonS3();

		String key = null;
		if (isBlank(path)) {
			String fileName = createFilename(file);
			key = "/" + fileName;
		} else {
			if (path.indexOf("/") == 0) {
				key = path;
			} else {
				key = "/" + path;
			}
		}
		try {
			logger.debug("Uploading a new object to S3 from a file\n");

			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);

			//set objcet access
			if (isPublicRead) {
				putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);//目前该参数不起作用，是否publicRead是由bucket的属性控制
			}

			s3.putObject(putObjectRequest);


			String s3Path = "http://storage.lianjia.com/" + bucketName + key;
			logger.info("Successfully uploaded object - " + s3Path);
			AmazonS3Response response = new AmazonS3Response(bucketName, key, s3Path);
			return response;
		} catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			logger.error("Error Message:    " + ase.getMessage());
			logger.error("HTTP Status Code: " + ase.getStatusCode());
			logger.error("AWS Error Code:   " + ase.getErrorCode());
			logger.error("Error Type:       " + ase.getErrorType());
			logger.error("Request ID:       " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, " + "such as not being able to access the network.");
			logger.error("Error Message: " + ace.getMessage());
			throw ace;
		} finally {
			closeAmazonS3Client(s3);
		}
	}

	public static void deleteObject(String fileName) throws AmazonClientException {
		String bucketName = getDefaultBucketName();
		deleteObject(bucketName, fileName);
	}

	public static void deleteObject(String bucketName, String fileName) throws AmazonClientException {
		AmazonS3Client s3 = getAmazonS3();
		String key = fileName;
		try {
			s3.deleteObject(bucketName, key);
		} catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			logger.error("Error Message:    " + ase.getMessage());
			logger.error("HTTP Status Code: " + ase.getStatusCode());
			logger.error("AWS Error Code:   " + ase.getErrorCode());
			logger.error("Error Type:       " + ase.getErrorType());
			logger.error("Request ID:       " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, " + "such as not being able to access the network.");
			logger.error("Error Message: " + ace.getMessage());
			throw ace;
		} finally {
			closeAmazonS3Client(s3);
		}
	}

	/**
	 * download files amazon s3 vitual service
	 *
	 * @param keyName
	 * @return
	 * @throws org.jets3t.service.ServiceException
	 *
	 */
	public static InputStream downLoad(String keyName) throws Exception {
		String bucketName = getDefaultBucketName();
		return downLoad(bucketName, keyName);
	}

	//	/**
	//	 * download files amazon s3 vitual service
	//	 *
	//	 * @param keyName
	//	 * @param width
	//	 * @param height
	//	 * @return
	//	 * @throws ServiceException
	//	 */
	//	public static InputStream downLoad(String keyName, String width, String height) throws ServiceException {
	//		// create S3Service
	//		S3Service s3Service = createS3Service();
	//		String bucketName = PropertyUtil.getProperty("s3service.s3-bucket-name");
	//		s3Service.getObject(bucketName, keyName, null, null, null, null, Long.valueOf(width), Long.valueOf(height));
	//		// Retrieve the whole data object we created previously
	//		S3Object objectComplete = s3Service.getObject(bucketName, keyName);
	//		return objectComplete.getDataInputStream();
	//	}

	/**
	 * download files amazon s3 vitual service
	 *
	 * @param bucketName
	 * @param keyName
	 * @return
	 * @throws IOException 
	 * @throws org.jets3t.service.ServiceException
	 *
	 */
	public static InputStream downLoad(String bucketName, String keyName) throws AmazonClientException, IOException {
		AmazonS3Client s3 = getAmazonS3();
		String key = keyName;
		try {
			S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
			logger.debug("Content-Type: " + object.getObjectMetadata().getContentType());
			InputStream is = object.getObjectContent();

			byte[] body = copyToByteArray(is);
			ByteArrayInputStream bis = new ByteArrayInputStream(body);
			return bis;
		} catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			logger.error("Error Message:    " + ase.getMessage());
			logger.error("HTTP Status Code: " + ase.getStatusCode());
			logger.error("AWS Error Code:   " + ase.getErrorCode());
			logger.error("Error Type:       " + ase.getErrorType());
			logger.error("Request ID:       " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, " + "such as not being able to access the network.");
			logger.error("Error Message: " + ace.getMessage());
			throw ace;
		} finally {
			closeAmazonS3Client(s3);
		}
	}

	/**
	 * create a file name
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static String createFilename(File file) {
		String fileName = UUID.randomUUID().toString();
		String currentName = file.getName();
		if (currentName.indexOf(".") > 0) {
			String lastSuffix = currentName.substring(currentName.lastIndexOf("."));
			return fileName + lastSuffix;
		}
		return fileName;
	}

	//	/**
	//	 * get AllBucket
	//	 *
	//	 * @return
	//	 * @throws S3ServiceException
	//	 */
	//	public static List<Bucket> getAllBucket() throws AmazonClientException {
	//		AmazonS3Client s3 = getAmazonS3();
	//
	//		try {
	//			return s3.listBuckets();
	//		} catch (AmazonServiceException ase) {
	//			logger.error("Caught an AmazonServiceException, which means your request made it "
	//					+ "to Amazon S3, but was rejected with an error response for some reason.");
	//			logger.error("Error Message:    " + ase.getMessage());
	//			logger.error("HTTP Status Code: " + ase.getStatusCode());
	//			logger.error("AWS Error Code:   " + ase.getErrorCode());
	//			logger.error("Error Type:       " + ase.getErrorType());
	//			logger.error("Request ID:       " + ase.getRequestId());
	//			throw ase;
	//		} catch (AmazonClientException ace) {
	//			logger.error("Caught an AmazonClientException, which means the client encountered "
	//					+ "a serious internal problem while trying to communicate with S3, " + "such as not being able to access the network.");
	//			logger.error("Error Message: " + ace.getMessage());
	//			throw ace;
	//		} finally{
	//				closeAmazonS3Client(s3);
	//			}
	//	}

	//	/**
	//	 * get all Object
	//	 *
	//	 * @param bucketName
	//	 * @return
	//	 * @throws S3ServiceException
	//	 */
	//	public static ObjectListing getObjectsByBucket(String bucketName) throws AmazonClientException {
	//		//		S3Service s3Service = createS3Service();
	//		//		return s3Service.listObjects(bucketName);
	//		AmazonS3Client s3 = getAmazonS3();
	//
	//		try {
	//			logger.debug("Listing objects");
	//			//            ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
	//			//                    .withBucketName(bucketName)
	//			//                    .withPrefix("My"));
	//			ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName));
	//			//            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
	//			//                logger.debug(" - " + objectSummary.getKey() + "  " +
	//			//                                   "(size = " + objectSummary.getSize() + ")");
	//			//            }
	//			//            logger.debug();
	//			return objectListing;
	//		} catch (AmazonServiceException ase) {
	//			logger.error("Caught an AmazonServiceException, which means your request made it "
	//					+ "to Amazon S3, but was rejected with an error response for some reason.");
	//			logger.error("Error Message:    " + ase.getMessage());
	//			logger.error("HTTP Status Code: " + ase.getStatusCode());
	//			logger.error("AWS Error Code:   " + ase.getErrorCode());
	//			logger.error("Error Type:       " + ase.getErrorType());
	//			logger.error("Request ID:       " + ase.getRequestId());
	//			throw ase;
	//		} catch (AmazonClientException ace) {
	//			logger.error("Caught an AmazonClientException, which means the client encountered "
	//					+ "a serious internal problem while trying to communicate with S3, " + "such as not being able to access the network.");
	//			logger.error("Error Message: " + ace.getMessage());
	//			throw ace;
	//		} finally{
	//	closeAmazonS3Client(s3);
	//}
	//	}

	public static boolean doesBucketExist(String bucketName) {
		AmazonS3Client s3 = getAmazonS3();

		try {
			return s3.doesBucketExist(bucketName);
		} catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			logger.error("Error Message:    " + ase.getMessage());
			logger.error("HTTP Status Code: " + ase.getStatusCode());
			logger.error("AWS Error Code:   " + ase.getErrorCode());
			logger.error("Error Type:       " + ase.getErrorType());
			logger.error("Request ID:       " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, " + "such as not being able to access the network.");
			logger.error("Error Message: " + ace.getMessage());
			throw ace;
		} finally {
			closeAmazonS3Client(s3);
		}
	}

	//	/**
	//	 * create a public bucket through set acl=public-read
	//	 *
	//	 * @param bucketName
	//	 * @throws org.jets3t.service.ServiceException
	//	 *
	//	 */
	//	public static void createBucket(String bucketName) throws AmazonClientException {
	//		AmazonS3Client s3 = getAmazonS3();
	//
	//		try {
	//			/*
	//			 * Create a new S3 bucket - Amazon S3 bucket names are globally
	//			 * unique, so once a bucket name has been taken by any user, you
	//			 * can't create another bucket with that same name.
	//			 *
	//			 * You can optionally specify a location for your bucket if you want
	//			 * to keep your data closer to your applications or users.
	//			 */
	//			logger.debug("Creating bucket " + bucketName + "\n");
	//			s3.createBucket(bucketName);
	//		} catch (AmazonServiceException ase) {
	//			logger.error("Caught an AmazonServiceException, which means your request made it "
	//					+ "to Amazon S3, but was rejected with an error response for some reason.");
	//			logger.error("Error Message:    " + ase.getMessage());
	//			logger.error("HTTP Status Code: " + ase.getStatusCode());
	//			logger.error("AWS Error Code:   " + ase.getErrorCode());
	//			logger.error("Error Type:       " + ase.getErrorType());
	//			logger.error("Request ID:       " + ase.getRequestId());
	//			throw ase;
	//		} catch (AmazonClientException ace) {
	//			logger.error("Caught an AmazonClientException, which means the client encountered "
	//					+ "a serious internal problem while trying to communicate with S3, " + "such as not being able to access the network.");
	//			logger.error("Error Message: " + ace.getMessage());
	//			throw ace;
	//		} finally{
	//				closeAmazonS3Client(s3);
	//			}
	//	}

	//	/**
	//	 * create or get a public bucket through set acl=public-read
	//	 *
	//	 * @param bucketName
	//	 * @throws ServiceException
	//	 */
	//	public static void createOrGetBucket(String bucketName) throws ServiceException {
	//		S3Service s3Service = createS3Service();
	//		s3Service.getOrCreateBucket(bucketName);
	//		S3Bucket publicBucket = new S3Bucket(bucketName);
	//		// Retrieve the bucket's ACL and modify it to grant public access,
	//		// ie READ access to the ALL_USERS group.
	//		AccessControlList bucketAcl = s3Service.getBucketAcl(publicBucket);
	//		bucketAcl.grantPermission(GroupGrantee.ALL_USERS, Permission.PERMISSION_READ);
	//		// Update the bucket's ACL. Now anyone can view the list of objects in
	//		// this bucket.
	//		publicBucket.setAcl(bucketAcl);
	//		s3Service.putBucketAcl(publicBucket);
	//	}

	/**
	 * get file mimetype from file
	 *
	 * @param file
	 * @return
	 * @throws java.io.IOException
	 */
	public static String getMimeType(File file) throws IOException {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(file));
			String mimeType = URLConnection.guessContentTypeFromStream(is);
			logger.debug("currentfile: " + file.getName() + " mimetype is : " + mimeType);
			return mimeType;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * get width and height from file
	 *
	 * @param file
	 * @return
	 * @throws java.io.IOException
	 */
	public static Map<String, Integer> getWidthHeight(File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		Map<String, Integer> response = new HashMap<String, Integer>();
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		response.put("width", width);
		response.put("height", height);
		return response;
	}

	/**
	 * download file to local
	 *
	 * @param inputStream
	 * @param localFilePath
	 */
	public static void downloadFile(InputStream inputStream, String localFilePath) {
		BufferedOutputStream bos = null;
		File file = new File(localFilePath);
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			int len = 2048;
			byte[] b = new byte[len];
			while ((len = inputStream.read(b)) != -1) {
				bos.write(b, 0, len);
			}
			bos.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * download file to local and return
	 *
	 * @param inputStream
	 * @param localFilePath
	 * @return
	 */
	public static File transferToFile(InputStream inputStream, String localFilePath) {
		BufferedOutputStream bos = null;
		File file = new File(localFilePath);
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			int len = 2048;
			byte[] b = new byte[len];
			while ((len = inputStream.read(b)) != -1) {
				bos.write(b, 0, len);
			}
			bos.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(inputStream);
		}
		return file;
	}

	public static String getUrlString(String bucketName, String key, Boolean isUrlExpire) {
		if (isUrlExpire) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, 30);
			Date expirationDate = cal.getTime();
			return generatePresignedUrlExpire(bucketName, key, expirationDate);
		}
		StringBuilder stringBuilder = new StringBuilder();
		if (protocolHttp) {
			stringBuilder.append("http://");
		} else {
			stringBuilder.append("https://");

		}
		stringBuilder.append(endpoint).append("/").append(bucketName).append("/").append(key);
		return stringBuilder.toString();
	}

	/**
	* The Example For The Parem: expirationDate
	*  Calendar cal = Calendar.getInstance();
	   cal.add(Calendar.YEAR, 200);
	   Date expirationDate = cal.getTime();
	* @param bucketName
	* @param key
	* @param expirationDate
	* @return
	*/
	public static String generatePresignedUrlExpire(String bucketName, String key, Date expirationDate) {
		GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, key);
		urlRequest.setExpiration(expirationDate);
		return generatePresignedUrl(urlRequest);
	}

	/**
	 * 生成带过期时间的 已存在的 bucket object URL ,该URL有授权的功能，同时支持上传和下载
	 * @return
	 */
	public static String generatePresignedUrl(GeneratePresignedUrlRequest urlRequest) {
		AmazonS3Client s3 = getAmazonS3();
		try {
			return s3.generatePresignedUrl(urlRequest).toString();
		} finally {
			closeAmazonS3Client(s3);
		}
	}

//	public static void main(String[] args) throws Exception {
//		File file = new File("D:\\huangbin\\Desktop\\企业微信截图_20181116163145.png");
//		System.out.println("width-height=" + getWidthHeight(file));
//		AmazonS3Response result = upload("fngte-image", "ste-web/file/12345.png", file, true);
//		System.out.println("result=" + result);
//	}
}
