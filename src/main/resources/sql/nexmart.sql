-- MySQL dump 10.13  Distrib 8.0.27, for Linux (x86_64)
--
-- Host: localhost    Database: nexmart_db
-- ------------------------------------------------------
-- Server version	8.0.27

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(32) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(11) NOT NULL COMMENT '收货人手机号',
  `province` varchar(32) DEFAULT NULL COMMENT '省份',
  `city` varchar(32) DEFAULT NULL COMMENT '城市',
  `district` varchar(32) DEFAULT NULL COMMENT '区/县',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否为默认地址（0否，1是）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (5,5,'丁真','16677889900','浙江省','杭州市','西湖区','678',1,'2026-03-30 20:59:07','2026-03-30 20:59:41'),(6,5,'雪豹','18388889999','福建省','厦门市','海沧区','4556567',0,'2026-03-30 20:59:37','2026-03-30 20:59:41'),(7,2,'Eric','18533333333','江苏省','无锡市','滨湖区','惠山路9号',1,'2026-04-02 16:23:15','2026-04-03 21:08:36'),(10,2,'qqq','17587788787','天津市','市辖区','和平区','r567yet567uyfrt',0,'2026-04-02 17:16:23','2026-04-22 14:43:33');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_message`
--

DROP TABLE IF EXISTS `ai_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '所属会话',
  `role` tinyint NOT NULL COMMENT '1=user 2=assistant',
  `content` text NOT NULL COMMENT '消息内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_message`
--

LOCK TABLES `ai_message` WRITE;
/*!40000 ALTER TABLE `ai_message` DISABLE KEYS */;
INSERT INTO `ai_message` VALUES (7,3,1,'这个网站有什么功能','2026-04-20 22:14:35'),(8,3,2,'NexMart商城提供商品浏览、搜索、购买、支付、订单管理、优惠活动参与等功能。您可以在首页查看热销商品、参与秒杀和领券，在个人中心管理地址、查看订单和积分。如需帮助，请随时告诉我！','2026-04-20 22:14:35'),(9,3,1,'我应该如何购买商品','2026-04-20 22:15:24'),(10,3,2,'在NexMart商城购买商品很简单：\n\n1. 先注册登录账号\n2. 在首页或分类页浏览商品，点击进入商品详情\n3. 选择规格（如有），点击\"加入购物车\"或\"立即购买\"\n4. 确认收货地址和优惠券，提交订单并支付\n\n支付成功后可在\"我的订单\"查看状态。所有商品免运费！','2026-04-20 22:15:24'),(11,3,1,'你是什么模型','2026-04-20 22:24:01'),(12,3,2,'我是Nex，NexMart商城的AI客服助手，专门为您解答商城使用、购物流程、优惠活动等相关问题。如果您有关于商品、订单或账户的任何疑问，我都很乐意帮助您！','2026-04-20 22:24:01'),(13,3,1,'真的假的','2026-04-20 22:34:35'),(14,3,2,'我是Nex，NexMart商城的官方AI客服助手，真实为您服务！如果您对商城功能或购物流程有任何疑问，我都可以提供准确帮助。需要了解什么具体功能吗？','2026-04-20 22:34:35'),(15,3,1,'今天有什么秒杀活动吗','2026-04-20 22:43:06'),(16,3,2,'今天有多个秒杀活动正在进行中：\n\n1. **秒杀测试** - 截止4月30日00时\n2. **商品秒杀活动** - 截止4月30日00时  \n3. **商品大促销** - 截止5月30日00时\n\n您可以在首页\"秒杀\"专区查看具体商品和优惠力度。秒杀商品库存有限，每人限购，记得准时参与哦！[DONE]','2026-04-20 22:43:06'),(17,3,1,'你知道我是谁吗','2026-04-21 14:28:53'),(18,3,2,'我是Nex，NexMart商城的AI助手。目前我无法识别您的具体身份信息，但很乐意为您提供购物帮助！如果您需要账户相关的服务，可以前往个人中心查看或联系人工客服哦~ 😊[DONE]','2026-04-21 14:28:53');
/*!40000 ALTER TABLE `ai_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_session`
--

DROP TABLE IF EXISTS `ai_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_session`
--

LOCK TABLES `ai_session` WRITE;
/*!40000 ALTER TABLE `ai_session` DISABLE KEYS */;
INSERT INTO `ai_session` VALUES (3,2,'2026-04-20 22:14:35','2026-04-20 22:14:35');
/*!40000 ALTER TABLE `ai_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '轮播图标题',
  `image_url` varchar(500) NOT NULL COMMENT '图片地址',
  `product_id` bigint NOT NULL COMMENT '关联商品ID',
  `sort` int DEFAULT '0' COMMENT '排序，数字越小越靠前',
  `status` tinyint DEFAULT '1' COMMENT '状态：0下架 1上架',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banner`
--

LOCK TABLES `banner` WRITE;
/*!40000 ALTER TABLE `banner` DISABLE KEYS */;
INSERT INTO `banner` VALUES (2,'13','',13,66,0,NULL,'2026-04-01 17:52:54'),(3,'555','',3,1,0,NULL,'2026-04-01 19:00:53'),(4,'666','',9,0,0,NULL,'2026-04-01 18:40:52'),(5,'777','',7,0,0,NULL,'2026-04-01 18:40:53'),(6,'888','',8,0,0,NULL,'2026-04-01 17:53:00'),(7,'999','',9,90,0,NULL,'2026-04-01 17:52:56'),(8,'吃乐事，有乐事','',11,10,1,'2026-04-01 17:10:54','2026-04-01 18:45:43'),(9,'678867','https://java-poke.oss-cn-beijing.aliyuncs.com/media/10970141cd0949f288305406f8b66c00.jpg',5,2,0,'2026-04-01 17:44:04','2026-04-01 18:57:20'),(10,'买苹果手机，享苹果人生','https://java-poke.oss-cn-beijing.aliyuncs.com/media/458b8331f44f410f82742465ddd24ab6.png',14,0,1,'2026-04-01 18:40:49','2026-04-02 12:55:45'),(11,'三折叠，怎么折，都有面','https://java-poke.oss-cn-beijing.aliyuncs.com/media/720bd15d80194e4b98a9aa8a457b276a.webp',15,1,1,'2026-04-01 18:55:29','2026-04-01 18:56:46'),(12,'【山姆】Member\'s Mark德国进口纯牛奶 200ml*10盒','',10,9,1,'2026-04-03 13:51:06','2026-04-21 18:32:01');
/*!40000 ALTER TABLE `banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车条目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `spec_id` bigint DEFAULT NULL COMMENT '规格id，逻辑关联 product_spec.id，无规格商品为null',
  `is_temporary` tinyint(1) DEFAULT '0' COMMENT '1=临时购物车,0=默认',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product_spec` (`user_id`,`product_id`,`spec_id`,`is_temporary`),
  KEY `idx_user_id` (`user_id`),
  KEY `fk_cart_product` (`product_id`),
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=196 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
INSERT INTO `cart_item` VALUES (97,5,9,3,'2026-03-31 13:02:34','2026-03-31 14:20:37',NULL,0),(109,5,11,3,'2026-04-01 20:11:07','2026-04-01 20:11:07',NULL,0);
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序值（越小越靠前）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0=隐藏 1=显示',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'数码电子',0,1,'2026-03-26 17:08:38','2026-03-26 17:08:38'),(2,'居家百货',6,1,'2026-03-26 17:08:38','2026-03-26 17:08:38'),(4,'精品图书',4,1,'2026-03-26 17:08:38','2026-04-22 14:44:42'),(6,'饮品专区',2,1,'2026-03-29 21:34:12','2026-03-29 21:34:12'),(7,'食品零食',3,1,'2026-03-29 21:36:50','2026-03-29 21:36:50'),(9,'新鲜水果',1,1,'2026-03-29 21:38:37','2026-03-29 21:38:37'),(10,'米面粮油',5,1,'2026-03-29 21:39:22','2026-03-29 21:39:22'),(12,'医药保健',8,1,'2026-03-29 21:41:04','2026-03-29 21:41:04'),(14,'鲜奶饮品',2,1,'2026-04-21 15:04:52','2026-04-21 15:04:52'),(15,'新鲜蔬菜',1,1,'2026-04-21 15:05:49','2026-04-21 15:05:49'),(16,'办公文具',4,1,'2026-04-21 15:06:48','2026-04-21 15:06:48'),(17,'家居电器',7,1,'2026-04-21 15:09:46','2026-04-21 15:09:46');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `checkin_points_rule`
--

DROP TABLE IF EXISTS `checkin_points_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `checkin_points_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `consecutive_days` int NOT NULL COMMENT '连续天数节点，0=普通签到',
  `points` int NOT NULL COMMENT '该节点获得积分',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='签到积分规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `checkin_points_rule`
--

LOCK TABLES `checkin_points_rule` WRITE;
/*!40000 ALTER TABLE `checkin_points_rule` DISABLE KEYS */;
INSERT INTO `checkin_points_rule` VALUES (1,0,10,'2026-04-21 22:46:41'),(2,3,15,'2026-04-16 09:31:46'),(3,7,30,'2026-04-16 08:15:25'),(4,15,50,'2026-04-16 08:15:25'),(5,30,100,'2026-04-16 08:15:25'),(6,90,333,'2026-04-16 23:01:25'),(7,180,666,'2026-04-16 23:01:37'),(8,365,1500,'2026-04-16 23:01:53');
/*!40000 ALTER TABLE `checkin_points_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon`
--

DROP TABLE IF EXISTS `coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '券名称',
  `coupon_type` tinyint NOT NULL COMMENT '券类型: 1=普通商品券 2=秒杀订单券',
  `discount_type` tinyint NOT NULL COMMENT '优惠方式: 1=满减 2=折扣 3=无门槛',
  `min_amount` decimal(10,2) DEFAULT '0.00' COMMENT '满减门槛金额(满减券用)',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '减免金额(满减/无门槛用)',
  `discount_rate` decimal(4,2) DEFAULT NULL COMMENT '折扣率 0.00~1.00(折扣券用)',
  `scope` tinyint DEFAULT '1' COMMENT '适用范围: 1=全场 2=单分类 3=单商品',
  `scope_id` bigint DEFAULT NULL COMMENT '分类ID或商品ID',
  `total` int NOT NULL COMMENT '发放总量, -1=不限量',
  `remained` int NOT NULL COMMENT '剩余可领数量',
  `per_limit` tinyint DEFAULT '1' COMMENT '每人限领张数',
  `receive_start` datetime NOT NULL COMMENT '领取开始时间',
  `receive_end` datetime NOT NULL COMMENT '领取截止时间',
  `receive_channel` tinyint DEFAULT NULL COMMENT '领取渠道,1=领券中心 2=积分商城 3=秒杀 null=暂无',
  `valid_days` int NOT NULL COMMENT '领取后N天内有效',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1=上架 0=下架',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon`
--

LOCK TABLES `coupon` WRITE;
/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` VALUES (5,'商品分类满减券',1,1,500.00,10.00,NULL,2,1,-1,2047483647,3,'2026-04-08 00:00:00','2026-05-06 00:00:00',1,7,1,'2026-04-09 15:14:38','2026-04-15 12:22:51'),(6,'商品全场满减券',1,1,1000.00,10.00,NULL,1,NULL,1000,990,10,'2026-04-02 00:00:00','2026-05-04 00:00:00',1,8,1,'2026-04-09 15:31:44','2026-04-15 12:22:51'),(7,'手机99折券',1,2,0.00,0.00,0.99,2,1,1000,999,1,'2026-04-08 00:00:00','2026-05-11 00:00:00',1,6,1,'2026-04-09 15:32:44','2026-04-15 12:22:51'),(8,'华为5元无门槛',1,3,0.00,5.00,NULL,3,15,1000,999,1,'2026-04-02 00:00:00','2026-04-23 00:00:00',1,6,1,'2026-04-09 15:33:38','2026-04-15 12:22:51'),(9,'水果大降价',1,1,10.00,1.00,NULL,2,9,1000,999,1,'2026-04-01 00:00:00','2026-05-11 00:00:00',1,7,1,'2026-04-09 17:57:22','2026-04-15 12:22:51'),(10,'零食清仓甩卖',1,3,0.00,1.00,NULL,2,7,1000,999,1,'2026-04-01 00:00:00','2026-05-26 00:00:00',1,7,1,'2026-04-09 18:41:01','2026-04-15 12:22:51'),(12,'3元无门槛积分订单券',2,3,0.00,3.00,NULL,1,NULL,1000,999,3,'2026-04-01 00:00:00','2026-05-31 00:00:00',2,7,1,'2026-04-12 16:47:53','2026-04-15 12:23:09'),(13,'95折积分订单券',2,2,0.00,0.00,0.95,1,NULL,1000,999,1,'2026-04-01 00:00:00','2026-05-31 00:00:00',2,7,1,'2026-04-12 16:48:52','2026-04-15 12:23:09'),(14,'test',1,1,5.00,1.00,NULL,1,NULL,1000,999,1,'2026-04-02 00:00:00','2026-05-27 00:00:00',1,7,1,'2026-04-12 19:08:21','2026-04-15 12:22:51'),(15,'秒杀订单券9折',2,2,0.00,0.00,0.90,1,NULL,1000,999,1,'2026-04-01 00:00:00','2026-05-22 00:00:00',3,7,1,'2026-04-13 22:57:12','2026-04-19 21:00:23'),(16,'test',2,3,0.00,9.00,NULL,1,NULL,9,7,1,'2026-04-01 00:00:00','2026-05-31 00:00:00',3,7,1,'2026-04-15 11:04:34','2026-04-19 22:37:34'),(18,'111',2,2,0.00,0.00,0.90,1,NULL,1000,998,1,'2026-04-08 00:00:00','2026-05-18 00:00:00',3,7,1,'2026-04-15 12:39:31','2026-04-20 16:24:04'),(19,'83折订单券',2,2,0.00,0.00,0.83,1,NULL,1000,1000,1,'2026-04-01 00:00:00','2026-04-03 00:00:00',3,7,1,'2026-04-15 20:18:31','2026-04-15 20:18:42'),(20,'111',1,2,0.00,0.00,0.99,1,NULL,1000,1000,1,'2026-04-01 00:00:00','2026-04-08 00:00:00',1,7,1,'2026-04-15 20:35:39','2026-04-15 20:35:39'),(21,'83折',2,2,0.00,0.00,0.83,1,NULL,1000,998,1,'2026-04-08 00:00:00','2026-05-17 00:00:00',3,7,1,'2026-04-15 21:08:46','2026-04-19 22:37:33'),(22,'好好好',2,2,0.00,0.00,0.99,1,NULL,1000,998,1,'2026-04-01 00:00:00','2026-05-26 00:00:00',3,7,1,'2026-04-19 21:11:49','2026-04-19 22:37:31'),(23,'积分9折券',2,2,0.00,0.00,0.90,1,NULL,1000,1000,1,'2026-04-01 00:00:00','2026-05-31 00:00:00',2,7,1,'2026-04-19 21:14:28','2026-04-19 21:14:38');
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon_user`
--

DROP TABLE IF EXISTS `coupon_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `coupon_id` bigint NOT NULL COMMENT '关联 coupon',
  `coupon_type` tinyint NOT NULL COMMENT '1=单一商品券 2=秒杀订单券',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0=未使用 1=已使用 2=已过期',
  `received_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `expire_at` datetime NOT NULL COMMENT '到期时间(领取时算好写入)',
  `used_at` datetime DEFAULT NULL COMMENT '使用时间',
  `order_id` bigint DEFAULT NULL COMMENT '使用时关联的订单ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_template` (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户持有优惠券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_user`
--

LOCK TABLES `coupon_user` WRITE;
/*!40000 ALTER TABLE `coupon_user` DISABLE KEYS */;
INSERT INTO `coupon_user` VALUES (1,2,6,1,1,'2026-04-09 17:15:38','2026-04-17 17:15:38','2026-04-10 18:55:12',48,'2026-04-09 17:15:37','2026-04-10 11:14:32'),(2,2,8,1,0,'2026-04-09 17:19:06','2026-04-15 17:19:06',NULL,NULL,'2026-04-09 17:19:04','2026-04-10 11:14:32'),(3,2,7,1,1,'2026-04-09 17:56:20','2026-04-15 17:56:20','2026-04-10 18:15:14',45,'2026-04-09 17:56:18','2026-04-10 11:14:32'),(4,2,9,1,1,'2026-04-09 17:58:00','2026-04-16 17:58:00','2026-04-10 18:34:52',46,'2026-04-09 17:57:59','2026-04-10 11:14:32'),(5,2,10,1,1,'2026-04-10 18:40:06','2026-04-17 18:40:06','2026-04-10 18:40:44',47,'2026-04-10 18:40:02','2026-04-10 18:40:02'),(6,2,5,1,1,'2026-04-10 18:41:44','2026-04-17 18:41:44','2026-04-12 18:46:09',54,'2026-04-10 18:41:41','2026-04-12 18:45:55'),(7,2,5,1,0,'2026-04-10 18:42:27','2026-04-17 18:42:27',NULL,NULL,'2026-04-10 18:42:24','2026-04-12 18:37:32'),(8,2,5,1,0,'2026-04-10 18:42:28','2026-04-17 18:42:28',NULL,NULL,'2026-04-10 18:42:25','2026-04-10 18:42:25'),(9,2,6,1,0,'2026-04-10 18:42:40','2026-04-18 18:42:40',NULL,NULL,'2026-04-10 18:42:37','2026-04-10 18:42:37'),(10,2,6,1,0,'2026-04-10 18:42:41','2026-04-18 18:42:41',NULL,NULL,'2026-04-10 18:42:37','2026-04-10 18:42:37'),(11,2,6,1,0,'2026-04-10 18:42:41','2026-04-18 18:42:41',NULL,NULL,'2026-04-10 18:42:38','2026-04-10 18:42:38'),(12,2,6,1,0,'2026-04-10 18:42:41','2026-04-18 18:42:41',NULL,NULL,'2026-04-10 18:42:38','2026-04-10 18:42:38'),(13,2,6,1,0,'2026-04-10 18:42:42','2026-04-18 18:42:42',NULL,NULL,'2026-04-10 18:42:38','2026-04-10 18:42:38'),(14,2,6,1,0,'2026-04-10 18:42:42','2026-04-18 18:42:42',NULL,NULL,'2026-04-10 18:42:39','2026-04-10 18:42:39'),(15,2,6,1,0,'2026-04-10 18:42:42','2026-04-18 18:42:42',NULL,NULL,'2026-04-10 18:42:39','2026-04-10 18:42:39'),(16,2,6,1,0,'2026-04-10 18:42:43','2026-04-18 18:42:43',NULL,NULL,'2026-04-10 18:42:39','2026-04-10 18:42:39'),(17,2,6,1,0,'2026-04-10 18:42:43','2026-04-18 18:42:43',NULL,NULL,'2026-04-10 18:42:40','2026-04-10 18:42:40'),(18,2,12,2,1,'2026-04-12 17:41:44','2026-04-19 17:41:44','2026-04-12 18:46:09',NULL,'2026-04-12 17:41:44','2026-04-12 18:45:55'),(19,2,13,2,0,'2026-04-12 19:14:22','2026-04-19 19:14:22',NULL,NULL,'2026-04-12 19:14:22','2026-04-19 19:28:50'),(20,2,21,2,1,'2026-04-15 21:22:22','2026-04-22 21:22:29','2026-04-15 23:40:30',NULL,'2026-04-15 21:22:22','2026-04-15 21:23:22'),(21,2,16,2,0,'2026-04-15 21:33:57','2026-04-22 21:34:04',NULL,NULL,'2026-04-15 21:33:57','2026-04-19 20:23:22'),(22,2,15,2,0,'2026-04-19 21:00:23','2026-04-26 21:00:25',NULL,NULL,'2026-04-19 21:00:23','2026-04-21 12:03:54'),(23,2,18,2,0,'2026-04-19 21:04:16','2026-04-26 21:04:17',NULL,NULL,'2026-04-19 21:04:16','2026-04-19 21:04:16'),(24,2,22,2,0,'2026-04-19 21:12:12','2026-04-26 21:12:13',NULL,NULL,'2026-04-19 21:12:12','2026-04-19 21:12:12'),(25,5,22,2,0,'2026-04-19 22:37:31','2026-04-26 22:37:33',NULL,NULL,'2026-04-19 22:37:31','2026-04-19 22:37:31'),(26,5,21,2,0,'2026-04-19 22:37:33','2026-04-26 22:37:35',NULL,NULL,'2026-04-19 22:37:33','2026-04-19 22:37:33'),(27,5,16,2,0,'2026-04-19 22:37:34','2026-04-26 22:37:36',NULL,NULL,'2026-04-19 22:37:34','2026-04-19 22:37:34'),(28,5,18,2,0,'2026-04-20 16:24:04','2026-04-27 16:24:13',NULL,NULL,'2026-04-20 16:24:04','2026-04-20 16:24:04'),(29,2,14,1,1,'2026-04-21 14:11:09','2026-04-28 14:11:09','2026-04-21 16:22:08',129,'2026-04-21 14:11:04','2026-04-21 14:26:28');
/*!40000 ALTER TABLE `coupon_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cs_message`
--

DROP TABLE IF EXISTS `cs_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cs_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '所属会话',
  `sender_type` tinyint NOT NULL COMMENT '1用户 2管理员',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `content` text COMMENT '消息内容',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '0未读 1已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1文字 2图片 3商品卡片 4订单卡片',
  `images` varchar(500) DEFAULT NULL COMMENT '图片URLs，逗号分隔',
  `product_id` bigint DEFAULT NULL COMMENT '关联商品ID',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  PRIMARY KEY (`id`),
  KEY `idx_session_type` (`session_id`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cs_message`
--

LOCK TABLES `cs_message` WRITE;
/*!40000 ALTER TABLE `cs_message` DISABLE KEYS */;
INSERT INTO `cs_message` VALUES (3,3,1,2,'你好',1,'2026-04-03 14:43:45',1,NULL,NULL,NULL),(4,3,1,2,'你好',1,'2026-04-03 14:51:04',1,NULL,NULL,NULL),(5,3,1,2,'1',1,'2026-04-05 17:55:10',1,NULL,NULL,NULL),(6,3,1,2,NULL,1,'2026-04-05 18:53:55',2,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/d683aa7db6064dcea2bc269e02ab01bb.png,https://java-poke.oss-cn-beijing.aliyuncs.com/media/93d7217069394f0c819cc233ee263b28.png',NULL,NULL),(7,3,1,2,NULL,1,'2026-04-05 19:00:54',3,NULL,14,NULL),(8,3,1,2,NULL,1,'2026-04-05 22:12:25',4,NULL,NULL,39),(9,3,1,2,NULL,1,'2026-04-05 22:12:50',4,NULL,NULL,31),(10,3,1,2,'1',1,'2026-04-05 22:17:16',1,NULL,NULL,NULL),(11,3,1,2,NULL,1,'2026-04-05 22:17:26',3,NULL,2,NULL),(12,3,1,2,'🤣🤣🤣',1,'2026-04-05 22:41:27',1,NULL,NULL,NULL),(13,3,1,1,'1',1,'2026-04-06 12:32:48',1,NULL,NULL,NULL),(14,3,1,1,'😅',1,'2026-04-06 12:33:07',1,NULL,NULL,NULL),(15,3,1,1,'111',1,'2026-04-06 12:34:24',1,NULL,NULL,NULL),(16,3,1,1,'222',1,'2026-04-06 12:34:34',1,NULL,NULL,NULL),(17,3,1,1,'😐111',1,'2026-04-06 12:39:41',1,NULL,NULL,NULL),(18,3,1,1,'111',1,'2026-04-06 12:40:01',1,NULL,NULL,NULL),(19,3,1,1,'111',1,'2026-04-06 12:42:37',1,NULL,NULL,NULL),(20,3,1,1,'11111',1,'2026-04-06 12:42:58',1,NULL,NULL,NULL),(21,3,1,1,'111111',1,'2026-04-06 12:46:18',1,NULL,NULL,NULL),(22,3,1,1,'55555',1,'2026-04-06 12:48:31',1,NULL,NULL,NULL),(23,3,1,3,'555',1,'2026-04-06 12:51:56',1,NULL,NULL,NULL),(24,3,1,3,'555',1,'2026-04-06 12:51:58',1,NULL,NULL,NULL),(25,3,2,3,'555',1,'2026-04-06 12:54:53',1,NULL,NULL,NULL),(26,3,1,1,'666',1,'2026-04-06 12:55:47',1,NULL,NULL,NULL),(27,3,1,1,'555',1,'2026-04-06 12:57:34',1,NULL,NULL,NULL),(28,3,2,1,'222',1,'2026-04-06 13:33:55',1,NULL,NULL,NULL),(29,3,2,3,'111',1,'2026-04-06 13:39:22',1,NULL,NULL,NULL),(30,3,2,3,'😥😥😥😥😛😛😛😛😛',1,'2026-04-06 13:41:30',1,NULL,NULL,NULL),(31,3,2,3,NULL,1,'2026-04-06 13:45:22',2,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/4fb71987518c4ed383f54921b21efc78.png',NULL,NULL),(32,3,2,3,NULL,1,'2026-04-06 13:45:22',2,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/8887b5c0e164456492b5197f2c90262c.png',NULL,NULL),(33,3,2,3,NULL,1,'2026-04-06 13:57:45',2,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/72264f1b955a401dbab7386016869d61.png',NULL,NULL),(34,3,2,3,NULL,1,'2026-04-06 13:57:45',2,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/a0e18ca1b48c4448bc80197fa8a68d7a.webp',NULL,NULL),(35,3,2,3,NULL,1,'2026-04-06 14:01:43',3,NULL,14,NULL),(36,3,2,3,'555',1,'2026-04-06 14:08:51',1,NULL,NULL,NULL),(37,3,2,3,'9',1,'2026-04-06 14:12:05',1,NULL,NULL,NULL),(38,3,2,3,'777',1,'2026-04-06 14:14:11',1,NULL,NULL,NULL),(39,3,2,3,'555',1,'2026-04-06 14:15:20',1,NULL,NULL,NULL),(40,3,2,3,'666',1,'2026-04-06 14:18:32',1,NULL,NULL,NULL),(41,3,2,3,'666',1,'2026-04-06 14:19:33',1,NULL,NULL,NULL),(42,3,2,3,'666',1,'2026-04-06 14:19:59',1,NULL,NULL,NULL),(43,3,2,3,'666',1,'2026-04-06 14:30:45',1,NULL,NULL,NULL),(44,3,2,3,'666',1,'2026-04-06 14:31:42',1,NULL,NULL,NULL),(45,3,2,3,'返回',1,'2026-04-06 14:57:03',1,NULL,NULL,NULL),(46,3,2,3,'777',1,'2026-04-06 14:57:51',1,NULL,NULL,NULL),(47,3,2,3,'666',1,'2026-04-06 14:59:10',1,NULL,NULL,NULL),(48,3,2,3,'666666',1,'2026-04-06 14:59:30',1,NULL,NULL,NULL),(49,3,1,2,'555',1,'2026-04-06 15:05:21',1,NULL,NULL,NULL),(50,3,1,2,'你好',1,'2026-04-06 15:07:40',1,NULL,NULL,NULL),(51,3,2,3,'666',1,'2026-04-06 15:07:45',1,NULL,NULL,NULL),(52,3,1,2,'111😄',1,'2026-04-06 15:08:09',1,NULL,NULL,NULL),(53,3,2,3,'你好',1,'2026-04-06 15:08:29',1,NULL,NULL,NULL),(54,3,1,2,'当前',1,'2026-04-06 15:09:19',1,NULL,NULL,NULL),(55,3,1,2,'我反而',1,'2026-04-06 15:12:26',1,NULL,NULL,NULL),(56,3,1,2,'1',1,'2026-04-06 15:12:53',1,NULL,NULL,NULL),(57,3,1,2,'1',1,'2026-04-06 15:13:02',1,NULL,NULL,NULL),(58,4,1,5,NULL,1,'2026-04-06 15:13:51',4,NULL,NULL,32),(59,4,1,5,'你好',1,'2026-04-06 15:13:58',1,NULL,NULL,NULL),(60,4,1,5,'你好',1,'2026-04-06 15:14:16',1,NULL,NULL,NULL),(61,4,1,5,'gsdfds',1,'2026-04-06 15:17:04',1,NULL,NULL,NULL),(62,4,1,5,'rgrgrsrge',1,'2026-04-06 15:17:07',1,NULL,NULL,NULL),(63,4,1,5,'rggrsrg',1,'2026-04-06 15:17:09',1,NULL,NULL,NULL),(64,4,1,5,'4554',1,'2026-04-06 15:23:13',1,NULL,NULL,NULL),(65,4,1,5,'ryturtyrt',1,'2026-04-06 15:23:18',1,NULL,NULL,NULL),(66,4,1,5,'dgdfgdfgdf',1,'2026-04-06 15:23:51',1,NULL,NULL,NULL),(67,4,1,5,'gdfgdfdfg',1,'2026-04-06 15:23:59',1,NULL,NULL,NULL),(68,4,1,5,'的感动感动',1,'2026-04-06 15:40:58',1,NULL,NULL,NULL),(69,4,1,5,'逛大润发发给',1,'2026-04-06 15:41:03',1,NULL,NULL,NULL),(70,3,1,2,'edrgh',1,'2026-04-06 15:46:31',1,NULL,NULL,NULL),(71,3,1,2,'fgdfgf',1,'2026-04-06 15:46:50',1,NULL,NULL,NULL),(72,3,1,2,'fsgf',1,'2026-04-06 15:46:55',1,NULL,NULL,NULL),(73,4,1,5,'😶😶😶',1,'2026-04-06 15:47:28',1,NULL,NULL,NULL),(74,4,1,5,'😏😏😏',1,'2026-04-06 15:47:36',1,NULL,NULL,NULL),(75,3,1,2,NULL,1,'2026-04-07 17:40:17',3,NULL,11,NULL),(76,3,1,2,'🤪',1,'2026-04-07 19:04:01',1,NULL,NULL,NULL),(77,3,1,2,'🤪',1,'2026-04-08 11:45:25',1,NULL,NULL,NULL),(78,3,1,2,NULL,1,'2026-04-08 11:48:33',4,NULL,NULL,33),(79,3,1,2,NULL,1,'2026-04-08 13:04:14',4,NULL,NULL,33),(80,3,1,2,NULL,1,'2026-04-08 15:01:43',4,NULL,NULL,42),(81,3,1,2,NULL,1,'2026-04-08 15:05:06',4,NULL,NULL,42),(82,3,1,2,NULL,1,'2026-04-08 15:16:34',4,NULL,NULL,41),(83,3,2,1,'你好',1,'2026-04-08 15:20:49',1,NULL,NULL,NULL),(84,3,2,1,'你好',1,'2026-04-08 15:20:53',1,NULL,NULL,NULL),(85,3,2,1,'你好',1,'2026-04-08 15:20:56',1,NULL,NULL,NULL),(86,3,2,1,'你好',1,'2026-04-08 15:28:54',1,NULL,NULL,NULL),(87,3,2,1,'你好',1,'2026-04-08 15:29:09',1,NULL,NULL,NULL),(88,3,2,1,'你好',1,'2026-04-08 15:29:29',1,NULL,NULL,NULL),(89,3,2,1,'你好',1,'2026-04-08 15:29:34',1,NULL,NULL,NULL),(90,3,1,2,'你好',1,'2026-04-08 15:30:01',1,NULL,NULL,NULL),(91,3,1,2,'你好',1,'2026-04-08 15:30:18',1,NULL,NULL,NULL),(92,3,1,2,'你好',1,'2026-04-08 15:30:31',1,NULL,NULL,NULL),(93,3,2,1,'你好',1,'2026-04-08 15:30:45',1,NULL,NULL,NULL),(94,3,2,1,'你好\\',1,'2026-04-08 15:34:31',1,NULL,NULL,NULL),(95,3,2,1,'你好',1,'2026-04-08 15:40:23',1,NULL,NULL,NULL),(96,4,2,1,'你好',1,'2026-04-08 15:40:29',1,NULL,NULL,NULL),(97,3,2,1,'你好',1,'2026-04-08 15:45:09',1,NULL,NULL,NULL),(98,3,2,1,'你好',1,'2026-04-08 15:45:22',1,NULL,NULL,NULL),(99,3,2,1,'1',1,'2026-04-08 23:03:30',1,NULL,NULL,NULL),(100,3,2,1,'1',1,'2026-04-08 23:03:32',1,NULL,NULL,NULL),(101,3,2,1,'1',1,'2026-04-08 23:03:33',1,NULL,NULL,NULL),(102,3,2,1,'1',1,'2026-04-08 23:13:01',1,NULL,NULL,NULL),(103,3,2,1,'1',1,'2026-04-08 23:13:03',1,NULL,NULL,NULL),(104,3,2,1,NULL,1,'2026-04-09 14:08:05',3,NULL,14,NULL),(105,3,1,2,NULL,1,'2026-04-11 13:12:46',4,NULL,NULL,48),(106,3,1,2,NULL,1,'2026-04-11 13:15:28',4,NULL,NULL,46),(107,3,1,2,NULL,1,'2026-04-11 13:17:41',4,NULL,NULL,48),(108,3,1,2,NULL,1,'2026-04-11 13:17:51',4,NULL,NULL,48),(109,3,1,2,NULL,1,'2026-04-11 13:22:52',4,NULL,NULL,48),(110,3,1,2,NULL,1,'2026-04-11 13:22:56',3,NULL,14,NULL),(111,3,1,2,NULL,1,'2026-04-12 18:46:48',4,NULL,NULL,54),(112,3,2,1,'😎',1,'2026-04-14 20:15:59',1,NULL,NULL,NULL),(113,3,1,2,NULL,1,'2026-04-15 22:16:18',4,NULL,NULL,57),(114,3,2,1,'666',1,'2026-04-20 22:36:23',1,NULL,NULL,NULL),(115,3,1,2,NULL,1,'2026-04-21 14:20:11',4,NULL,NULL,128),(116,3,1,2,'1',1,'2026-04-21 14:21:20',1,NULL,NULL,NULL),(117,3,1,2,'1',1,'2026-04-21 14:21:28',1,NULL,NULL,NULL);
/*!40000 ALTER TABLE `cs_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cs_session`
--

DROP TABLE IF EXISTS `cs_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cs_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发起用户',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1进行中 2已关闭',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cs_session`
--

LOCK TABLES `cs_session` WRITE;
/*!40000 ALTER TABLE `cs_session` DISABLE KEYS */;
INSERT INTO `cs_session` VALUES (3,2,1,'2026-04-03 14:43:37','2026-04-03 14:43:37'),(4,5,1,'2026-04-06 15:13:35','2026-04-06 15:13:35');
/*!40000 ALTER TABLE `cs_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `home_section`
--

DROP TABLE IF EXISTS `home_section`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `home_section` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `section_type` tinyint NOT NULL COMMENT '模块类型：1热销商品 2新品上市 3为你推荐',
  `auto_mode` tinyint DEFAULT '1' COMMENT '0手动 1自动',
  `auto_limit` int DEFAULT '10' COMMENT '自动模式取前N条',
  `status` tinyint DEFAULT '1' COMMENT '0禁用 1启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `home_section`
--

LOCK TABLES `home_section` WRITE;
/*!40000 ALTER TABLE `home_section` DISABLE KEYS */;
INSERT INTO `home_section` VALUES (1,1,0,10,1),(2,2,1,12,1),(3,3,1,16,1);
/*!40000 ALTER TABLE `home_section` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `home_section_item`
--

DROP TABLE IF EXISTS `home_section_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `home_section_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `section_type` tinyint NOT NULL,
  `product_id` bigint NOT NULL,
  `sort` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `home_section_item`
--

LOCK TABLES `home_section_item` WRITE;
/*!40000 ALTER TABLE `home_section_item` DISABLE KEYS */;
INSERT INTO `home_section_item` VALUES (5,1,14,1),(6,3,13,1),(7,1,11,4),(9,1,10,5),(10,1,63,2),(11,1,56,6),(12,1,45,7),(13,1,35,8),(14,1,3,3);
/*!40000 ALTER TABLE `home_section_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号（全局唯一）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `address_id` bigint DEFAULT NULL COMMENT '关联的地址ID（用于溯源）',
  `seckill_item_id` bigint DEFAULT NULL COMMENT '秒杀商品ID，非秒杀订单为null',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0=取消 1=待付款 2=待发货 3=待收货 4=完成 ',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态: 0=未支付 1=已支付',
  `original_amount` decimal(10,2) NOT NULL COMMENT '订单总金额(原价)',
  `final_amount` decimal(10,2) DEFAULT NULL COMMENT '最终实付金额',
  `order_coupon_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用的订单券名称',
  `promotion_total_discount` decimal(10,2) DEFAULT NULL COMMENT '活动总计优惠金额',
  `product_coupon_total_discount` decimal(10,2) DEFAULT NULL COMMENT '商品券优惠金额合计',
  `order_coupon_discount` decimal(10,2) DEFAULT NULL,
  `seckill_discount` decimal(10,2) DEFAULT NULL COMMENT '秒杀优惠金额',
  `receiver_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人手机号',
  `address` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货地址',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `complete_time` datetime DEFAULT NULL COMMENT '订单完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (1,'20260327170318169448',2,NULL,NULL,1,0,21394.00,21394.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-27 17:03:18','2026-04-10 17:24:04',NULL),(2,'20260327170545893136',2,NULL,NULL,0,0,5198.00,5198.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-27 17:05:44','2026-04-10 17:24:04',NULL),(3,'20260327185456494424',2,NULL,NULL,0,0,2796.50,2796.50,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-27 18:54:55','2026-04-10 17:24:04',NULL),(4,'20260327185537611923',2,NULL,NULL,0,0,2796.50,2796.50,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-27 18:55:36','2026-04-10 17:24:04',NULL),(5,'20260327190022094103',2,NULL,NULL,1,0,7697.50,7697.50,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','1','2026-03-27 19:00:21','2026-04-10 17:24:04',NULL),(6,'20260327190634782077',2,4,NULL,1,0,1698.50,1698.50,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-27 19:06:33','2026-04-10 17:24:04',NULL),(7,'20260327190935035465',2,4,NULL,1,0,598.00,598.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-27 19:09:34','2026-04-10 17:24:04',NULL),(8,'20260327191315589518',2,4,NULL,1,0,598.00,598.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-27 19:13:14','2026-04-10 17:24:04',NULL),(9,'20260328092105871729',2,4,NULL,0,0,199.00,199.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 09:21:00','2026-04-10 17:24:04',NULL),(10,'20260328094333871796',2,4,NULL,1,0,45.00,45.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 09:43:28','2026-04-10 17:24:04',NULL),(11,'20260328094447746206',2,4,NULL,0,0,199.00,199.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 09:44:41','2026-04-10 17:24:04',NULL),(12,'20260328095551197024',2,4,NULL,0,0,199.00,199.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 09:55:45','2026-04-10 17:24:04',NULL),(13,'20260328095700226281',2,4,NULL,1,0,45.00,45.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 09:56:54','2026-04-10 17:24:04',NULL),(14,'20260328100119969344',2,4,NULL,1,0,399.00,399.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 10:01:13','2026-04-10 17:24:04',NULL),(15,'20260328100330300699',2,4,NULL,1,0,399.00,399.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 10:03:25','2026-04-10 17:24:04',NULL),(16,'20260328100631713873',2,4,NULL,1,0,399.00,399.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 10:06:25','2026-04-10 17:24:04',NULL),(17,'20260328101056675693',2,4,NULL,1,0,399.00,399.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 10:10:50','2026-04-10 17:24:04',NULL),(18,'20260328103147108662',2,4,NULL,1,0,399.00,399.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 10:31:41','2026-04-10 17:24:04',NULL),(19,'20260328103355125263',2,4,NULL,1,0,399.00,399.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 10:33:48','2026-04-10 17:24:04',NULL),(20,'20260328144558821997',2,4,NULL,1,0,1999.00,1999.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 14:45:58','2026-04-10 17:24:04',NULL),(21,'20260328194921038976',2,4,NULL,1,0,1.00,1.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 19:49:18','2026-04-10 17:24:04',NULL),(22,'20260328200426455229',2,4,NULL,1,0,1.00,1.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 20:04:23','2026-04-10 17:24:04',NULL),(23,'20260328200931589762',2,4,NULL,1,0,1.00,1.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 20:09:28','2026-04-10 17:24:04',NULL),(24,'20260328201320709406',2,4,NULL,4,1,1.00,1.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-28 20:13:17','2026-04-10 17:24:04','2026-04-01 13:25:46'),(25,'20260329131123014443',2,4,NULL,4,1,400.00,400.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-29 13:11:21','2026-04-10 17:24:04','2026-04-03 13:54:00'),(26,'20260329203228388301',2,4,NULL,0,0,1.00,1.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-29 20:32:23','2026-04-10 17:24:04',NULL),(27,'20260329205401878752',2,4,NULL,4,1,1.00,1.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-29 20:53:56','2026-04-10 17:24:04','2026-04-02 19:23:15'),(28,'20260329230041788290',2,4,NULL,4,1,100.00,100.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-29 23:00:36','2026-04-10 17:24:04','2026-03-29 18:25:14'),(29,'20260330180347193410',2,4,NULL,0,0,15.00,15.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-30 18:03:44','2026-04-10 17:24:04',NULL),(30,'20260330180939992537',2,4,NULL,4,1,11.00,11.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-30 18:09:36','2026-04-10 17:24:04','2026-04-01 13:24:23'),(31,'20260330205549391903',2,4,NULL,0,1,30.00,30.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-03-30 20:55:45','2026-04-10 17:24:04',NULL),(32,'20260330210002288017',5,5,NULL,4,1,15.00,15.00,NULL,NULL,NULL,NULL,NULL,'丁真','16677889900','浙江省杭州市西湖区678','2134','2026-03-30 20:59:58','2026-04-20 21:48:02','2026-04-02 13:24:23'),(33,'20260401103303128722',2,4,NULL,4,1,10.00,10.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-04-01 10:33:02','2026-04-10 17:24:04','2026-04-01 10:41:18'),(34,'20260401104421699631',2,4,NULL,3,1,20.00,20.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-04-01 10:44:20','2026-04-10 17:24:04',NULL),(35,'20260401104912923494',2,4,NULL,4,1,5.00,5.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-04-01 10:49:11','2026-04-10 17:24:04','2026-04-01 10:51:49'),(36,'20260401105432574758',2,4,NULL,4,1,5.00,5.00,NULL,NULL,NULL,NULL,NULL,'trer','18888888888','山东省济南市历下区fgddfgfgfd','','2026-04-01 10:54:31','2026-04-10 17:24:04','2026-04-01 10:55:40'),(37,'20260402174449744962',2,7,NULL,0,0,21.00,21.00,NULL,NULL,NULL,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-02 17:44:46','2026-04-10 17:24:04',NULL),(38,'20260402200259040107',2,7,NULL,2,1,21.00,21.00,NULL,NULL,NULL,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','我去','2026-04-02 20:03:02','2026-04-10 17:24:04',NULL),(39,'20260402200459185641',2,7,NULL,3,1,5.00,5.00,NULL,NULL,NULL,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','qqq','2026-04-02 20:05:02','2026-04-10 17:24:04',NULL),(41,'20260407230538037569',2,7,NULL,1,0,20.00,20.00,NULL,NULL,NULL,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-07 23:05:32','2026-04-10 17:24:04',NULL),(42,'20260407230708542436',2,7,NULL,4,1,21.00,18.60,NULL,NULL,NULL,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-07 23:07:02','2026-04-10 17:24:04','2026-04-08 10:22:35'),(45,'20260410181513347367',2,7,NULL,1,0,8.00,7.19,NULL,0.80,0.01,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-10 18:15:11','2026-04-10 18:33:23',NULL),(46,'20260410183451507753',2,7,NULL,4,2,12.00,9.80,NULL,1.20,1.00,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-10 18:34:48','2026-04-10 19:26:44','2026-04-10 19:26:45'),(47,'20260410184043351477',2,7,NULL,0,0,10.00,8.00,NULL,1.00,1.00,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-10 18:40:41','2026-04-10 20:55:08',NULL),(48,'20260410185512586836',2,7,NULL,4,1,10399.00,7389.00,NULL,3000.00,10.00,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-10 18:55:09','2026-04-10 19:27:24','2026-04-10 19:27:25'),(49,'20260412181922840577',2,7,NULL,0,0,4.00,1.00,NULL,0.00,0.00,3.00,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-12 18:19:18','2026-04-12 18:22:28',NULL),(50,'20260412182318003585',2,7,NULL,0,0,1999.00,1986.00,NULL,0.00,10.00,3.00,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-12 18:23:13','2026-04-12 18:37:32',NULL),(51,'20260412183756838178',2,7,NULL,0,0,1999.00,1986.00,NULL,0.00,10.00,3.00,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-12 18:37:51','2026-04-12 18:39:57',NULL),(52,'20260412184028764794',2,7,NULL,0,0,1999.00,1986.00,NULL,0.00,10.00,3.00,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-12 18:40:24','2026-04-12 18:41:35',NULL),(53,'20260412184205448957',2,7,NULL,0,0,1999.00,1986.00,NULL,0.00,10.00,3.00,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-12 18:42:00','2026-04-12 18:45:55',NULL),(54,'20260412184609019218',2,7,NULL,1,0,1999.00,1986.00,'3元无门槛积分订单券',0.00,10.00,3.00,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-12 18:46:04','2026-04-12 18:46:04',NULL),(55,'SK20260415194846297450',2,7,5,0,0,10399.00,6666.00,NULL,NULL,NULL,NULL,3733.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-15 19:48:41','2026-04-15 19:49:00',NULL),(56,'SK20260415200853481641',2,7,5,0,0,10399.00,6666.00,NULL,NULL,NULL,NULL,3733.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-15 20:08:48','2026-04-15 20:09:30',NULL),(57,'SK20260415214308710432',2,7,5,4,1,10399.00,6666.00,NULL,NULL,NULL,NULL,3733.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-15 21:43:02','2026-04-15 21:44:32','2026-04-15 21:44:39'),(58,'20260415234029647689',2,7,NULL,1,0,5.00,4.15,'83折',0.00,0.00,0.85,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-15 23:40:22','2026-04-15 23:40:22',NULL),(59,'20260419183848588274',2,7,NULL,0,0,5.00,4.75,NULL,0.00,0.00,0.25,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 18:38:42','2026-04-19 18:38:52',NULL),(60,'20260419184135228263',2,7,NULL,0,0,5.00,4.75,NULL,0.00,0.00,0.25,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 18:41:29','2026-04-19 18:43:44',NULL),(61,'20260419184406106662',2,7,NULL,0,0,5.00,4.75,NULL,0.00,0.00,0.25,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 18:43:59','2026-04-19 18:48:12',NULL),(62,'20260419184836434034',2,7,NULL,0,0,5.00,4.75,NULL,0.00,0.00,0.25,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 18:48:29','2026-04-19 18:48:40',NULL),(63,'20260419185242113224',2,7,NULL,0,0,5.00,4.75,NULL,0.00,0.00,0.25,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 18:52:36','2026-04-19 18:52:46',NULL),(64,'20260419185706827085',2,7,NULL,0,0,5.00,4.75,NULL,0.00,0.00,0.25,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 18:57:00','2026-04-19 18:57:10',NULL),(65,'20260419190041290151',2,7,NULL,0,0,5.00,4.75,NULL,0.00,0.00,0.25,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 19:00:34','2026-04-19 19:00:44',NULL),(66,'20260419191350895965',2,7,NULL,0,0,5.00,4.75,NULL,0.00,0.00,0.25,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 19:13:50','2026-04-19 19:28:50',NULL),(67,'20260419194212586099',2,7,NULL,0,0,5.00,5.00,NULL,0.00,0.00,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 19:42:12','2026-04-19 19:57:12',NULL),(68,'20260419200044910133',2,7,NULL,0,0,45.00,36.00,NULL,0.00,0.00,9.00,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 20:00:44','2026-04-19 20:02:56',NULL),(69,'20260419200322670946',2,7,NULL,0,0,45.00,36.00,NULL,0.00,0.00,9.00,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 20:03:21','2026-04-19 20:08:12',NULL),(70,'20260419200822076789',2,7,NULL,0,0,45.00,36.00,'test',0.00,0.00,9.00,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-19 20:08:22','2026-04-19 20:23:22',NULL),(71,'SK20260419214944075955',2,7,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-19 21:49:43','2026-04-19 22:04:43',NULL),(72,'SK20260419215029045632',2,7,12,0,0,5.00,3.00,NULL,NULL,NULL,NULL,2.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-19 21:50:28','2026-04-19 22:05:28',NULL),(73,'SK20260419215119473821',2,7,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-19 21:51:18','2026-04-19 22:06:18',NULL),(74,'SK20260419215228263098',2,7,14,0,0,1999.00,199.00,NULL,NULL,NULL,NULL,1800.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-19 21:52:27','2026-04-19 22:07:27',NULL),(75,'SK20260419215235600428',2,7,14,0,0,1999.00,199.00,NULL,NULL,NULL,NULL,1800.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-19 21:52:34','2026-04-19 22:07:34',NULL),(76,'SK20260419220214177823',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:02:13','2026-04-19 22:17:13',NULL),(77,'SK20260419220302319581',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:03:01','2026-04-19 22:18:01',NULL),(78,'SK20260419220308136119',5,5,14,0,0,1999.00,199.00,NULL,NULL,NULL,NULL,1800.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:03:07','2026-04-19 22:18:07',NULL),(79,'SK20260419220312842907',5,5,14,0,0,1999.00,199.00,NULL,NULL,NULL,NULL,1800.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:03:11','2026-04-19 22:18:11',NULL),(80,'SK20260419220316547379',5,5,14,0,0,1999.00,199.00,NULL,NULL,NULL,NULL,1800.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:03:15','2026-04-19 22:18:15',NULL),(81,'SK20260419220332118815',5,5,12,0,0,5.00,3.00,NULL,NULL,NULL,NULL,2.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:03:31','2026-04-19 22:18:31',NULL),(82,'SK20260419220337394588',5,5,12,0,0,5.00,3.00,NULL,NULL,NULL,NULL,2.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:03:36','2026-04-19 22:18:36',NULL),(83,'SK20260419220436065188',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:04:35','2026-04-19 22:19:35',NULL),(84,'SK20260419220450913668',5,5,5,0,0,10399.00,6666.00,NULL,NULL,NULL,NULL,3733.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:04:49','2026-04-19 22:19:49',NULL),(85,'SK20260419221411227829',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:14:10','2026-04-19 22:29:10',NULL),(86,'SK20260419222849979512',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:28:48','2026-04-19 22:43:48',NULL),(87,'SK20260419222852739206',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:28:50','2026-04-19 22:43:50',NULL),(88,'SK20260419222858372059',5,5,5,0,0,10399.00,6666.00,NULL,NULL,NULL,NULL,3733.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:28:56','2026-04-19 22:43:57',NULL),(89,'SK20260419222902771405',5,5,12,0,0,5.00,3.00,NULL,NULL,NULL,NULL,2.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:29:01','2026-04-19 22:44:01',NULL),(90,'SK20260419222915456603',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:29:14','2026-04-19 22:44:14',NULL),(91,'SK20260419223424540445',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:34:23','2026-04-19 22:49:23',NULL),(92,'SK20260419223426629599',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:34:25','2026-04-19 22:49:25',NULL),(93,'SK20260419223554769295',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:35:52','2026-04-19 22:50:52',NULL),(94,'SK20260419223558709261',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:35:57','2026-04-19 22:50:57',NULL),(95,'SK20260419223600747696',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:35:58','2026-04-19 22:50:58',NULL),(96,'SK20260419223602327835',5,5,12,0,0,5.00,3.00,NULL,NULL,NULL,NULL,2.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 22:36:01','2026-04-19 22:51:01',NULL),(97,'SK20260419231438762899',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:14:36','2026-04-19 23:14:42',NULL),(98,'SK20260419231454767737',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:14:53','2026-04-19 23:14:59',NULL),(99,'SK20260419231525718070',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:15:24','2026-04-19 23:15:30',NULL),(100,'SK20260419231535585799',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:15:33','2026-04-19 23:15:39',NULL),(101,'SK20260419231536950324',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:15:35','2026-04-19 23:15:41',NULL),(102,'SK20260419231655562822',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:16:54','2026-04-19 23:17:14',NULL),(103,'SK20260419231723011690',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:17:21','2026-04-19 23:17:41',NULL),(104,'SK20260419231725119643',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:17:23','2026-04-19 23:17:43',NULL),(105,'SK20260419231726861786',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:17:25','2026-04-19 23:17:45',NULL),(106,'SK20260419231728121768',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:17:27','2026-04-19 23:17:47',NULL),(107,'SK20260419231730679414',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:17:28','2026-04-19 23:17:48',NULL),(108,'SK20260419231826110565',5,5,5,0,0,10399.00,6666.00,NULL,NULL,NULL,NULL,3733.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:25','2026-04-19 23:18:45',NULL),(109,'SK20260419231829170683',5,5,12,0,0,5.00,3.00,NULL,NULL,NULL,NULL,2.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:28','2026-04-19 23:18:48',NULL),(110,'SK20260419231831682251',5,5,12,0,0,5.00,3.00,NULL,NULL,NULL,NULL,2.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:29','2026-04-19 23:18:49',NULL),(111,'SK20260419231832469648',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:30','2026-04-19 23:18:50',NULL),(112,'SK20260419231833080441',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:32','2026-04-19 23:18:52',NULL),(113,'SK20260419231835348675',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:33','2026-04-19 23:18:53',NULL),(114,'SK20260419231836615259',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:35','2026-04-19 23:18:55',NULL),(115,'SK20260419231838521320',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:36','2026-04-19 23:18:56',NULL),(116,'SK20260419231839762964',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:37','2026-04-19 23:18:58',NULL),(117,'SK20260419231840769682',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:39','2026-04-19 23:18:59',NULL),(118,'SK20260419231842168273',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-19 23:18:41','2026-04-19 23:19:01',NULL),(119,'SK20260420155935215131',5,5,5,0,0,10399.00,6666.00,NULL,NULL,NULL,NULL,3733.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-20 15:59:27','2026-04-20 16:14:27',NULL),(120,'SK20260420160011723853',5,5,12,0,0,5.00,3.00,NULL,NULL,NULL,NULL,2.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-20 16:00:03','2026-04-20 16:15:03',NULL),(121,'SK20260420160013534831',5,5,12,0,0,5.00,3.00,NULL,NULL,NULL,NULL,2.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-20 16:00:04','2026-04-20 16:15:04',NULL),(122,'SK20260420160017943516',5,5,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-20 16:00:09','2026-04-20 16:15:09',NULL),(123,'SK20260420160019645809',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-20 16:00:11','2026-04-20 16:15:11',NULL),(124,'SK20260420160021242348',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-20 16:00:12','2026-04-20 16:15:12',NULL),(125,'SK20260420160022985114',5,5,15,0,0,199.00,10.00,NULL,NULL,NULL,NULL,189.00,'丁真','16677889900','浙江省杭州市西湖区678',NULL,'2026-04-20 16:00:14','2026-04-20 16:15:14',NULL),(126,'SK20260420163940440485',2,7,13,0,0,1.00,1.00,NULL,NULL,NULL,NULL,0.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-20 16:39:32','2026-04-20 16:54:32',NULL),(127,'20260421114857785546',2,7,NULL,0,0,27.13,24.42,'秒杀订单券9折',0.00,0.00,2.71,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-21 11:48:54','2026-04-21 12:03:54',NULL),(128,'20260421141132492035',2,7,NULL,0,0,20.00,17.00,NULL,2.00,1.00,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-21 14:11:28','2026-04-21 14:26:28',NULL),(129,'20260421162207480945',2,7,NULL,4,1,12.67,10.40,NULL,1.27,1.00,NULL,NULL,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号','','2026-04-21 16:22:03','2026-04-21 16:26:32','2026-04-21 16:26:38'),(130,'SK20260421173339498868',2,7,13,0,0,111.88,99.00,NULL,NULL,NULL,NULL,12.88,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-21 17:33:33','2026-04-21 17:48:34',NULL),(131,'SK20260421225601224927',2,7,23,0,0,13.80,9.90,NULL,NULL,NULL,NULL,3.90,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-21 22:55:56','2026-04-21 23:10:56',NULL),(132,'SK20260421225616081461',2,7,21,0,0,11.40,9.90,NULL,NULL,NULL,NULL,1.50,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-21 22:56:11','2026-04-21 23:11:11',NULL),(133,'SK20260421225627205396',2,7,19,0,0,5999.00,5555.00,NULL,NULL,NULL,NULL,444.00,'Eric','18533333333','江苏省无锡市滨湖区惠山路9号',NULL,'2026-04-21 22:56:22','2026-04-21 23:11:22',NULL);
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID（冗余，商品删除后仍可查）',
  `product_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下单时商品名称（冗余快照）',
  `price` decimal(10,2) NOT NULL COMMENT '下单时单价（冗余快照）',
  `promotional_price` decimal(10,2) DEFAULT NULL COMMENT '活动优惠后的单价',
  `coupon_discount` decimal(10,2) DEFAULT NULL COMMENT '商品券优惠金额',
  `seckill_price` decimal(10,2) DEFAULT NULL COMMENT '秒杀单价快照',
  `promotion_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '享受的促销活动名',
  `coupon_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用的优惠券名',
  `seckill_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '秒杀活动名称快照',
  `quantity` int NOT NULL COMMENT '购买数量',
  `spec_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下单时的规格名称快照',
  `cover_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下单时商品封面（冗余快照）',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=158 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,1,1,'旗舰智能手机 Pro',5999.00,NULL,NULL,NULL,NULL,NULL,NULL,3,NULL,'https://oss-example.com/covers/phone.jpg'),(2,1,2,'无线降噪耳机',1299.50,NULL,NULL,NULL,NULL,NULL,NULL,2,NULL,'https://oss-example.com/covers/headphones.jpg'),(3,1,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,2,NULL,'https://oss-example.com/covers/bedding.jpg'),(4,2,2,'无线降噪耳机',1299.50,NULL,NULL,NULL,NULL,NULL,NULL,4,NULL,'https://oss-example.com/covers/headphones.jpg'),(5,3,2,'无线降噪耳机',1299.50,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(6,3,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(7,3,5,'智能电热水壶',199.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/kettle.jpg'),(8,3,6,'人体工学办公椅',899.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/chair.jpg'),(9,4,2,'无线降噪耳机',1299.50,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(10,4,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(11,4,5,'智能电热水壶',199.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/kettle.jpg'),(12,4,6,'人体工学办公椅',899.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/chair.jpg'),(13,5,1,'旗舰智能手机 Pro',5999.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/phone.jpg'),(14,5,2,'无线降噪耳机',1299.50,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(15,5,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(16,6,2,'无线降噪耳机',1299.50,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(17,6,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(18,7,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(19,7,5,'智能电热水壶',199.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/kettle.jpg'),(20,8,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(21,8,5,'智能电热水壶',199.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/kettle.jpg'),(22,9,5,'智能电热水壶',199.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/kettle.jpg'),(23,10,8,'《MySQL必知必会》',45.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/book_mysql.jpg'),(24,11,5,'智能电热水壶',199.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/kettle.jpg'),(25,12,5,'智能电热水壶',199.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/kettle.jpg'),(26,13,8,'《MySQL必知必会》',45.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/book_mysql.jpg'),(27,14,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(28,15,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(29,16,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(30,17,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(31,18,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(32,19,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(33,20,3,'二代智能手表',1999.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/watch.jpg'),(34,21,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(35,22,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(36,23,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(37,24,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(38,25,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(39,25,4,'全棉简约四件套',399.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/bedding.jpg'),(40,26,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(41,27,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(42,28,10,'山姆牛奶2L-户晨风严选',33.00,NULL,NULL,NULL,NULL,NULL,NULL,3,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/f558fece699d4baa8d92ca4b29ebc76b.png'),(43,28,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(44,29,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,3,'番茄味',''),(45,30,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://oss-example.com/covers/headphones.jpg'),(46,30,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,2,'烤肉味',''),(47,31,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,3,'番茄味',''),(48,31,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,3,'烤肉味',''),(49,32,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,3,'番茄味',''),(50,33,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,2,'黄瓜味',''),(51,34,13,'苹果5斤',20.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'红富士','https://java-poke.oss-cn-beijing.aliyuncs.com/media/29d29e6eb7fa4b0a99db85a8ee6a6cbc.jpg'),(52,35,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味',''),(53,36,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味',''),(54,37,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,''),(55,37,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,2,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(56,37,9,'苹果3斤',10.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/a849ef72ac8f4520aea5b97a3dd5fab3.jpg'),(57,38,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,''),(58,38,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,2,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(59,38,9,'苹果3斤',10.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/a849ef72ac8f4520aea5b97a3dd5fab3.jpg'),(60,39,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(63,41,2,'无线降噪耳机',1.00,0.85,NULL,NULL,'电子产品大促销',NULL,NULL,5,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(64,41,11,'乐事薯片60g',5.00,4.50,NULL,NULL,'百亿补贴',NULL,NULL,3,'番茄味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(65,42,2,'无线降噪耳机',1.00,0.85,NULL,NULL,'电子产品大促销',NULL,NULL,6,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(66,42,11,'乐事薯片60g',5.00,4.50,NULL,NULL,'百亿补贴',NULL,NULL,3,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(67,45,2,'无线降噪耳机',1.00,0.90,0.01,NULL,'百亿补贴','手机99折券',NULL,3,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(68,45,11,'乐事薯片60g',5.00,4.50,NULL,NULL,'百亿补贴',NULL,NULL,1,'番茄味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(69,46,2,'无线降噪耳机',1.00,0.90,NULL,NULL,'百亿补贴',NULL,NULL,2,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(70,46,9,'苹果3斤',10.00,9.00,1.00,NULL,'百亿补贴','水果大降价',NULL,1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/a849ef72ac8f4520aea5b97a3dd5fab3.jpg'),(71,47,11,'乐事薯片60g',5.00,4.50,1.00,NULL,'百亿补贴','零食清仓甩卖',NULL,2,'番茄味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(72,48,14,'IPhone 17 Pro Max',10399.00,7399.00,10.00,NULL,'苹果促销','商品全场满减券',NULL,1,'沪橙风','https://java-poke.oss-cn-beijing.aliyuncs.com/media/06db8dea0cfc45739cd08e898a9171a3.png'),(73,49,2,'无线降噪耳机',1.00,NULL,NULL,NULL,NULL,NULL,NULL,4,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(74,50,3,'二代智能手表',1999.00,NULL,10.00,NULL,NULL,'商品分类满减券',NULL,1,NULL,''),(75,51,3,'二代智能手表',1999.00,NULL,10.00,NULL,NULL,'商品分类满减券',NULL,1,NULL,''),(76,52,3,'二代智能手表',1999.00,NULL,10.00,NULL,NULL,'商品分类满减券',NULL,1,NULL,''),(77,53,3,'二代智能手表',1999.00,NULL,10.00,NULL,NULL,'商品分类满减券',NULL,1,NULL,''),(78,54,3,'二代智能手表',1999.00,NULL,10.00,NULL,NULL,'商品分类满减券',NULL,1,NULL,''),(79,55,14,'IPhone 17 Pro Max',10399.00,NULL,NULL,6666.00,NULL,NULL,'商品秒杀活动',1,'沪橙风','https://java-poke.oss-cn-beijing.aliyuncs.com/media/06db8dea0cfc45739cd08e898a9171a3.png'),(80,56,14,'IPhone 17 Pro Max',10399.00,NULL,NULL,6666.00,NULL,NULL,'商品秒杀活动',1,'深蓝色','https://java-poke.oss-cn-beijing.aliyuncs.com/media/06db8dea0cfc45739cd08e898a9171a3.png'),(81,57,14,'IPhone 17 Pro Max',10399.00,NULL,NULL,6666.00,NULL,NULL,'商品秒杀活动',1,'沪橙风','https://java-poke.oss-cn-beijing.aliyuncs.com/media/06db8dea0cfc45739cd08e898a9171a3.png'),(82,58,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(83,59,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(84,60,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(85,61,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(86,62,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(87,63,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(88,64,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(89,65,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(90,66,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(91,67,11,'乐事薯片60g',5.00,NULL,NULL,NULL,NULL,NULL,NULL,1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(92,68,8,'《MySQL必知必会》',45.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,''),(93,69,8,'《MySQL必知必会》',45.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,''),(94,70,8,'《MySQL必知必会》',45.00,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,''),(95,71,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(96,72,11,'乐事薯片60g',5.00,NULL,NULL,3.00,NULL,NULL,'商品秒杀活动',1,'烤肉味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(97,73,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(98,74,3,'二代智能手表',1999.00,NULL,NULL,199.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(99,75,3,'二代智能手表',1999.00,NULL,NULL,199.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(100,76,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(101,77,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(102,78,3,'二代智能手表',1999.00,NULL,NULL,199.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(103,79,3,'二代智能手表',1999.00,NULL,NULL,199.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(104,80,3,'二代智能手表',1999.00,NULL,NULL,199.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(105,81,11,'乐事薯片60g',5.00,NULL,NULL,3.00,NULL,NULL,'商品秒杀活动',1,'柠檬味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(106,82,11,'乐事薯片60g',5.00,NULL,NULL,3.00,NULL,NULL,'商品秒杀活动',1,'柠檬味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(107,83,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(108,84,14,'IPhone 17 Pro Max',10399.00,NULL,NULL,6666.00,NULL,NULL,'商品秒杀活动',1,'沪橙风','https://java-poke.oss-cn-beijing.aliyuncs.com/media/06db8dea0cfc45739cd08e898a9171a3.png'),(109,85,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(110,86,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(111,87,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(112,88,14,'IPhone 17 Pro Max',10399.00,NULL,NULL,6666.00,NULL,NULL,'商品秒杀活动',1,'沪橙风','https://java-poke.oss-cn-beijing.aliyuncs.com/media/06db8dea0cfc45739cd08e898a9171a3.png'),(113,89,11,'乐事薯片60g',5.00,NULL,NULL,3.00,NULL,NULL,'商品秒杀活动',1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(114,90,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(115,91,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(116,92,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(117,93,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(118,94,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(119,95,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(120,96,11,'乐事薯片60g',5.00,NULL,NULL,3.00,NULL,NULL,'商品秒杀活动',1,'柠檬味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(121,97,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(122,98,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(123,99,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(124,100,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(125,101,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(126,102,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(127,103,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(128,104,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(129,105,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(130,106,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(131,107,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(132,108,14,'IPhone 17 Pro Max',10399.00,NULL,NULL,6666.00,NULL,NULL,'商品秒杀活动',1,'沪橙风','https://java-poke.oss-cn-beijing.aliyuncs.com/media/06db8dea0cfc45739cd08e898a9171a3.png'),(133,109,11,'乐事薯片60g',5.00,NULL,NULL,3.00,NULL,NULL,'商品秒杀活动',1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(134,110,11,'乐事薯片60g',5.00,NULL,NULL,3.00,NULL,NULL,'商品秒杀活动',1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(135,111,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(136,112,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(137,113,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(138,114,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(139,115,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(140,116,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(141,117,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(142,118,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(143,119,14,'IPhone 17 Pro Max',10399.00,NULL,NULL,6666.00,NULL,NULL,'商品秒杀活动',1,'沪橙风','https://java-poke.oss-cn-beijing.aliyuncs.com/media/06db8dea0cfc45739cd08e898a9171a3.png'),(144,120,11,'乐事薯片60g',5.00,NULL,NULL,3.00,NULL,NULL,'商品秒杀活动',1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(145,121,11,'乐事薯片60g',5.00,NULL,NULL,3.00,NULL,NULL,'商品秒杀活动',1,'原味','https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png'),(146,122,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(147,123,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(148,124,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(149,125,5,'智能电热水壶',199.00,NULL,NULL,10.00,NULL,NULL,'商品秒杀活动',1,NULL,''),(150,126,2,'无线降噪耳机',1.00,NULL,NULL,1.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/853e13ba0e994ce79724f88749d5537b.png'),(151,127,21,'三只松鼠每日坚果400g*1',27.13,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/bc79034c63ba4cd9a3b75bfa30fa605b.png'),(152,128,13,'陕西红富士苹果冰糖心5斤',20.00,18.00,1.00,NULL,'百亿补贴','test',NULL,1,'红富士','https://java-poke.oss-cn-beijing.aliyuncs.com/media/998b2b314177440bba126a111a2f4738.png'),(153,129,49,'小懒财富自由之路',12.67,11.40,1.00,NULL,'百亿补贴','test',NULL,1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/eea23ac9a04e45858d2bda8149e8ff23.png'),(154,130,2,'【漫步者】Z1 Air+真无线蓝牙耳机',111.88,NULL,NULL,99.00,NULL,NULL,'商品秒杀活动',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/60ec6cf1a26847d394489e28eda96ce2.png'),(155,131,25,'【卫龙】魔芋爽 15包混合装',13.80,NULL,NULL,9.90,NULL,NULL,'零食秒杀',1,NULL,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/9605b74f103c4d2996b1f3146e19fe72.png'),(156,132,26,'芬达500ml*5瓶',11.40,NULL,NULL,9.90,NULL,NULL,'清凉夏日',1,'经典','https://java-poke.oss-cn-beijing.aliyuncs.com/media/e847a2caad18473d94f57315c3a09ef4.png'),(157,133,63,'【OPPO】 FindX9Pro 16GB+512GB',5999.00,NULL,NULL,5555.00,NULL,NULL,'618秒杀',1,'霜白','https://java-poke.oss-cn-beijing.aliyuncs.com/media/d622d78962ff474fb3027c63d9c6256c.png');
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `pay_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付宝交易流水号',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `pay_type` tinyint NOT NULL DEFAULT '1' COMMENT '支付方式: 1=支付宝',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0=未支付 1=已支付 2=已退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_pay_no` (`pay_no`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,9,'20260328092105871729',NULL,199.00,1,0,NULL,'2026-03-28 09:21:04','2026-03-28 09:21:04'),(2,10,'20260328094333871796',NULL,45.00,1,0,NULL,'2026-03-28 09:43:29','2026-03-28 09:43:29'),(3,11,'20260328094447746206',NULL,199.00,1,0,NULL,'2026-03-28 09:44:45','2026-03-28 09:44:45'),(4,12,'20260328095551197024',NULL,199.00,1,0,NULL,'2026-03-28 09:55:47','2026-03-28 09:55:47'),(5,13,'20260328095700226281',NULL,45.00,1,0,NULL,'2026-03-28 09:56:56','2026-03-28 09:56:56'),(6,14,'20260328100119969344',NULL,399.00,1,0,NULL,'2026-03-28 10:01:14','2026-03-28 10:01:14'),(7,15,'20260328100330300699',NULL,399.00,1,0,NULL,'2026-03-28 10:03:27','2026-03-28 10:03:27'),(8,16,'20260328100631713873',NULL,399.00,1,0,NULL,'2026-03-28 10:06:30','2026-03-28 10:06:30'),(9,17,'20260328101056675693',NULL,399.00,1,0,NULL,'2026-03-28 10:10:55','2026-03-28 10:10:55'),(10,18,'20260328103147108662',NULL,399.00,1,0,NULL,'2026-03-28 10:31:42','2026-03-28 10:31:42'),(11,19,'20260328103355125263',NULL,399.00,1,0,NULL,'2026-03-28 10:33:49','2026-03-28 10:33:49'),(12,20,'20260328144558821997',NULL,1999.00,1,0,NULL,'2026-03-28 19:45:29','2026-03-28 19:45:29'),(13,21,'20260328194921038976',NULL,1.00,1,0,NULL,'2026-03-28 19:49:20','2026-03-28 19:49:20'),(14,22,'20260328200426455229',NULL,1.00,1,0,NULL,'2026-03-28 20:04:31','2026-03-28 20:04:31'),(15,23,'20260328200931589762_15',NULL,1.00,1,0,NULL,'2026-03-28 20:09:30','2026-03-28 20:09:30'),(16,24,'20260328201320709406_16','2026032822001460030508867260',1.00,1,1,'2026-03-28 20:13:44','2026-03-28 20:13:19','2026-03-28 20:13:40'),(17,25,'20260329131123014443_17',NULL,400.00,1,0,NULL,'2026-03-29 13:11:29','2026-03-29 13:11:29'),(18,26,'20260329203228388301_18',NULL,1.00,1,0,NULL,'2026-03-29 20:32:40','2026-03-29 20:32:40'),(19,27,'20260329205401878752_19',NULL,1.00,1,0,NULL,'2026-03-29 20:53:58','2026-03-29 20:53:58'),(20,28,'20260329230041788290_20',NULL,100.00,1,0,NULL,'2026-03-29 23:00:38','2026-03-29 23:00:38'),(21,30,'20260330180939992537_21',NULL,11.00,1,0,NULL,'2026-03-30 18:09:44','2026-03-30 18:09:45'),(22,31,'20260330205549391903_22',NULL,30.00,1,0,NULL,'2026-03-30 20:55:46','2026-03-30 20:55:46'),(23,32,'20260330210002288017_23',NULL,15.00,1,0,NULL,'2026-03-30 21:00:02','2026-03-30 21:00:02'),(24,33,'20260401103303128722_24','2026040122001460030508903380',10.00,1,1,'2026-04-01 10:34:33','2026-04-01 10:33:04','2026-04-01 10:34:31'),(25,34,'20260401104421699631_25','2026040122001460030508904449',20.00,1,1,'2026-04-01 10:45:17','2026-04-01 10:44:23','2026-04-01 10:45:15'),(26,35,'20260401104912923494_26','2026040122001460030508904450',5.00,1,1,'2026-04-01 10:49:40','2026-04-01 10:49:12','2026-04-01 10:49:38'),(27,36,'20260401105432574758_27','2026040122001460030508907544',5.00,1,1,'2026-04-01 10:55:05','2026-04-01 10:54:32','2026-04-01 10:55:03'),(28,38,'20260402200259040107_28','2026040222001460030508921046',21.00,1,1,'2026-04-02 20:03:44','2026-04-02 20:03:05','2026-04-02 20:03:47'),(29,39,'20260402200459185641_29','2026040222001460030508919759',5.00,1,1,'2026-04-02 20:05:40','2026-04-02 20:05:20','2026-04-02 20:05:43'),(30,47,'20260410184043351477_30',NULL,8.00,1,0,NULL,'2026-04-10 18:41:00','2026-04-10 18:41:00'),(31,129,'20260421162207480945_31','2026042122001460030509110348',10.40,1,1,'2026-04-21 16:22:59','2026-04-21 16:22:17','2026-04-21 16:22:53');
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `points_mall_item`
--

DROP TABLE IF EXISTS `points_mall_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `points_mall_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NOT NULL COMMENT '关联优惠券模板ID',
  `points_cost` int NOT NULL COMMENT '兑换所需积分',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=上架 2=下架',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分商城兑换项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `points_mall_item`
--

LOCK TABLES `points_mall_item` WRITE;
/*!40000 ALTER TABLE `points_mall_item` DISABLE KEYS */;
INSERT INTO `points_mall_item` VALUES (2,12,5,1,'2026-04-12 16:48:06','2026-04-12 16:48:06'),(3,13,10,1,'2026-04-12 16:49:10','2026-04-12 16:49:10'),(6,23,1,1,'2026-04-19 21:14:38','2026-04-19 21:14:38');
/*!40000 ALTER TABLE `points_mall_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '售价（元）',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `cover_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图OSS地址',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0=下架 1=上架 2=售空',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `has_spec` tinyint DEFAULT '0' COMMENT '0=无规格 1=有规格',
  `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (2,1,'【漫步者】Z1 Air+真无线蓝牙耳机','【漫步者】Z1 Air+真无线蓝牙耳机蓝牙6.0AI翻译耳机通话降噪苹果安卓',111.88,5,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/60ec6cf1a26847d394489e28eda96ce2.png',1,'2026-03-26 17:09:31','2026-04-21 17:48:34',0,10),(3,1,'Apple Watch Series 11 GPS版','Apple/苹果 Apple Watch Series 11 GPS版运动表带高端智能手表',2230.00,5,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/046bd0ef397e4e6984073a67ca2f0c33.png',1,'2026-03-26 17:09:31','2026-04-21 16:54:05',1,0),(4,2,'全棉简约四件套','北欧风全棉纯100%棉床上用品三四件套被套宿舍单人被套床单笠套件',132.00,3,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/4008261c005349a4853a5953ad4b4f58.png',1,'2026-03-26 17:09:31','2026-04-21 13:57:28',0,0),(5,17,'【奥克斯】官方正品电热水壶1.6L','【奥克斯】官方正品电热水壶一体成型烧水壶宿舍美观无缝一键保温家用',39.90,29,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/372ea17b202c432dbd2f659bfaa974cb.png',1,'2026-03-26 17:09:31','2026-04-21 15:10:18',0,0),(7,1,'【狼途】T88客制化机械键盘静音轴','【狼途】T88客制化机械键盘静音轴无线蓝牙有线键盘侧刻电竞游戏通用',279.99,50,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/91c213d553434d41bb59a93188b90b44.png',1,'2026-03-26 17:09:31','2026-04-21 13:52:55',0,0),(8,4,'MySQL数据库应用案例教程 ','MySQL数据库应用案例教程 数据库数据表MySQL常用函数 十四五教材',39.80,5,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/8e4a011df84542edb537afb1dbbbd233.png',1,'2026-03-26 17:09:31','2026-04-21 13:43:46',0,0),(9,9,'山西红富士苹果3斤','【脆甜】正宗山西红富士苹果水果脆甜冰糖心当季新鲜水果整箱批发',10.00,47,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/1e15ea2c2d4847b98fb8c0761dfc99ef.png',1,'2026-03-29 21:42:49','2026-04-21 13:26:29',0,1),(10,14,'【山姆】Member\'s Mark德国进口纯牛奶 200ml*10盒','【山姆】Member\'s Mark德国进口 全脂牛乳(灭菌乳) 纯牛奶 200ml*10盒',31.30,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/f0fd4f06cbd04b4e90248e1787da8205.png',1,'2026-03-29 21:48:15','2026-04-21 16:38:33',0,3),(11,7,'乐事薯片60g','超便宜',5.00,68,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/610c599128d74f6485cf44b4b608d2cb.png',1,'2026-03-30 12:43:05','2026-04-20 16:15:04',1,5),(12,12,'21金维他补钙60片','',33.00,26,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/9c4a76be1dcd4d729b36399427ffdefd.png',1,'2026-03-30 13:51:04','2026-04-21 14:03:53',0,0),(13,9,'陕西红富士苹果冰糖心5斤','【产地直发】正宗陕西红富士苹果冰糖心脆甜孕妇新鲜水果整箱批发',20.00,63,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/998b2b314177440bba126a111a2f4738.png',1,'2026-03-31 12:52:03','2026-04-21 14:26:28',1,100),(14,1,'IPhone 17 Pro Max 256G','苹果 iPhone17 ProMax全网通5G 双卡双待手机',9999.00,11,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/a261408d2d304466b32a0e17d1d6185d.png',1,'2026-04-01 18:38:18','2026-04-21 16:58:56',1,2),(15,1,'HUAWEI Mate XTs 非凡大师 16GB+256GB','HUAWEI Mate XTs 非凡大师 三折叠手机 鸿蒙智能手机',17999.00,28,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/e38d85d9faf943109eb2fca9f80015a8.webp',1,'2026-04-01 18:52:45','2026-04-21 16:57:20',1,0),(21,7,'三只松鼠每日坚果400g*1','【纯坚果】三只松鼠每日坚果400g*1罐装干果坚果休闲零食特产送礼',27.13,100,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/bc79034c63ba4cd9a3b75bfa30fa605b.png',1,'2026-04-21 10:45:17','2026-04-21 12:03:54',0,0),(22,7,'士力架小金块110g','【新品】士力架小金块110g盒装花生夹心巧克力能量棒零食10g/根',8.48,100,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/10315518d0394fd89990d00774ce5866.png',1,'2026-04-21 10:49:10','2026-04-21 10:49:10',0,0),(23,7,'【阿宽】超大袋红油面皮 5袋','【阿宽】红油面皮麻酱面皮干拌面方便面泡面宽凉皮擀面皮懒人充饥面食',16.45,99,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/ee863b2041d344ae8b4a15a27412dad2.png',1,'2026-04-21 11:07:59','2026-04-21 11:33:57',1,0),(24,7,'【彩虹】糖9g每袋15包混合装','【彩虹】糖9g袋装脆皮软糖混合味*15包孩子喜爱童趣糖果零食批发',41.99,100,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/1d4ef118d94149aa9ca28d587b1b5340.png',1,'2026-04-21 11:52:40','2026-04-21 13:22:21',0,0),(25,7,'【卫龙】魔芋爽 15包混合装','【卫龙】魔芋爽素毛肚馋魔芋麻辣香辣条素肉休闲即食解馋小零食批发',13.80,95,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/9605b74f103c4d2996b1f3146e19fe72.png',1,'2026-04-21 12:03:11','2026-04-21 22:48:57',0,0),(26,6,'芬达500ml*5瓶','【可口可乐】无糖芬达500ml*5瓶经典口味无糖橙味汽水碳酸饮料包邮',11.40,105,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/e847a2caad18473d94f57315c3a09ef4.png',1,'2026-04-21 12:48:06','2026-04-21 22:45:03',1,0),(27,6,'【百事可乐】无糖300ml*5瓶','【百事可乐】迷你瓶无糖300ml*5瓶装碳酸汽水经典饮品解渴饮料',5.00,100,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/7f58d6a7f93c47ccba83167ccb766a5c.png',1,'2026-04-21 12:50:52','2026-04-21 12:50:52',0,0),(28,6,'雪碧柠檬茶500ml *2瓶','【可口可乐】雪碧柠檬茶经典柠檬红茶风味汽水500ml碳酸饮料网红饮料',7.30,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/61e69cfbac34449bb9e1181291640fbc.png',1,'2026-04-21 12:53:17','2026-04-21 13:22:50',0,0),(29,6,'5瓶康师傅冰红茶330ml','5瓶康师傅冰红茶330ml清爽解腻饮料经典口味饮料小瓶便携',7.30,1000,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/63fc4c966f5243ad8e58cc8c3c45cf60.png',1,'2026-04-21 12:55:30','2026-04-21 13:23:03',0,0),(30,6,'美汁源系列盲盒5瓶装500ml','【可口可乐】美汁源系列盲盒饮料5瓶装随机搭配的特价包邮',15.00,100,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/0f1f24156d5e484cbe8cac6148201083.png',1,'2026-04-21 12:58:42','2026-04-21 13:20:10',0,0),(31,14,'伊利纯牛奶200ml*8盒','11月产伊利纯牛奶200ml*8盒装全脂纯牛奶饮料家庭尊享装散装特价清仓',10.70,100,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/231ce58f883742fba9b3bb4a73849ff3.png',1,'2026-04-21 13:02:38','2026-04-21 15:07:42',0,0),(32,14,'蒙牛新养道200ml*12包一整箱','3月产蒙牛新养道维生素ADE无乳糖全脂牛奶饮料200ml*12包一整箱正品',29.80,110,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/98e57fc6f3fa493db0c51840611283d5.png',1,'2026-04-21 13:12:45','2026-04-21 15:07:37',0,0),(33,9,'云南露天新鲜蓝莓半斤','现摘现发云南露天新鲜蓝莓 当季时令孕妇水果 非怡颗秘鲁进口蓝莓',13.10,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/125b402f6d6d49c59b19e43a8e39de9b.png',1,'2026-04-21 13:14:39','2026-04-21 13:14:39',0,0),(34,9,'冰糖雪梨5斤','【正宗雪梨】新鲜梨子水果整箱包邮河北赵县雪花梨冰糖雪梨当应季',12.90,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/0fe74fafeec94e94b3f90f8483609b80.png',1,'2026-04-21 13:16:11','2026-04-21 13:16:11',0,0),(35,9,'伦晚脐橙【带箱5斤】','【带箱5斤】伦晚脐橙新鲜橙子当季甜橙现摘应季水果榨汁橙一整箱',8.80,33,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/262e5ed5aab948a3ab7cd1892939b714.png',1,'2026-04-21 13:17:29','2026-04-21 13:17:29',0,0),(36,9,'越南白心火龙果进口3斤','【精选大果】越南白心火龙果进口热带水果甘甜爽口糖宝可食速发',11.33,5,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/f22d88d897bf435a8c8bdecdfa074056.png',1,'2026-04-21 13:18:46','2026-04-21 13:18:46',0,0),(37,9,'香蕉5斤','【自然熟单根香蕉】高山大香蕉青皮青蕉发货自然熟香甜整箱孕妇甜',13.50,5,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/41d050cc96c940f09eee968bd03abfdd.png',1,'2026-04-21 13:21:59','2026-04-21 13:21:59',0,0),(38,15,'新鲜娃娃菜耕野精品3颗1斤','云南高山新鲜娃娃菜耕野精品黄心嫩滑爽口火锅涮菜批发三颗小包装',4.67,55,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/71f733db79734c50b39a6563cda7fea6.png',1,'2026-04-21 13:29:06','2026-04-21 15:07:52',0,0),(39,10,'【金龙鱼】伴手礼','【金龙鱼】伴手礼油米面套装食用油大米挂面公司团购送礼节日年货礼盒',15.00,15,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/8ebf9a78712648b6a55c3c842a4bf297.png',1,'2026-04-21 13:33:37','2026-04-21 13:33:37',0,0),(40,10,'中粮1斤大米','中粮福临门大米',3.00,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/71255dff776a4671946c0f519e18679b.png',1,'2026-04-21 13:35:09','2026-04-21 16:36:09',0,0),(41,10,'【金龙鱼】高筋麦芯小麦粉1kg','【金龙鱼】高筋麦芯小麦粉1kg袋装家用包子饺子馒头手擀面高筋面粉',5.84,12,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/e25c73d52a78457ca9456edffe2b1978.png',1,'2026-04-21 13:36:21','2026-04-21 13:36:21',0,0),(42,10,'【金龙鱼】浓香花生油5L1桶','【金龙鱼】浓香花生油5L/桶装物理压榨一级花生油煎煮炒菜家用食用油',89.90,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/a6a1e73ca34942a3a0dce6ab8402c827.png',1,'2026-04-21 13:37:40','2026-04-21 13:37:40',0,0),(43,16,'a4文件夹3个','a4文件夹多层学生用透明插页试卷整理神器学生书风琴包分类试卷夹',11.70,11,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/6f3bf68243194e2c9cda6843a229f5f9.png',1,'2026-04-21 13:40:59','2026-04-21 15:08:09',0,0),(44,16,'【得力】金属圆规','【得力】金属圆规考研专业制图绘图圆规学生专用考试圆规画圆尺规套装',12.90,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/e2128b3664b945eab93a40f031273e48.png',1,'2026-04-21 13:42:11','2026-04-21 15:08:13',0,0),(45,4,'新航道 雅思真词汇 全新升级版','新航道 雅思真词汇 全新升级版 赠音频 剑3-剑20词汇 雅思考试',24.50,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/cd886d1a18894933b741e3f033876ddd.png',1,'2026-04-21 13:45:13','2026-04-21 13:45:13',0,0),(46,4,'新东方 看漫画学日语：N2语法考前对策 当当','新东方 看漫画学日语：N2语法考前对策 当当',17.80,5,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/8b2916fdf96b4a68a54990c0151e656e.png',1,'2026-04-21 13:46:06','2026-04-21 13:46:06',0,0),(47,4,'2025新版高顿CMA中文教材','【官方正版】2025新版高顿CMA中文教材高顿2025CMA教材财务规划',29.50,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/1e30a1c8ff174798825c46c9670d4b20.png',1,'2026-04-21 13:47:05','2026-04-21 13:47:05',0,0),(48,4,'【2册】AI金融+财务工具量化交易大数据算法分析','【2册】AI金融+财务工具量化交易大数据算法分析投资经济学原理财',26.48,5,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/3e6df3bb0ad64579b93874396c17ad4e.png',1,'2026-04-21 13:48:36','2026-04-21 13:48:36',0,0),(49,4,'小懒财富自由之路','小懒财富自由之路基金投资入门与技巧家庭理财书籍定投金融股票',12.67,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/eea23ac9a04e45858d2bda8149e8ff23.png',1,'2026-04-21 13:50:08','2026-04-21 16:27:52',0,1),(50,17,'【康佳】电煮锅学生宿舍家用','【康佳】电煮锅学生宿舍家用多功能小型一体火锅不粘锅迷你电热电炒锅',39.80,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/0b339370c17b4d708a69adccb1fddb30.png',1,'2026-04-21 13:58:38','2026-04-21 15:10:23',0,0),(51,17,'桌面风扇超静音小型','桌面风扇超静音小型办公室小风扇台式大风力电风扇充电学生宿舍',39.99,9,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/2bc61ee3d0284c6d94ba606e0ba82981.png',1,'2026-04-21 13:59:59','2026-04-21 15:10:27',0,0),(52,12,'康恩贝维生素C泡腾片20片2瓶','【2支】康恩贝维生素C泡腾片20片补充营养富含VC7种水果口味正品',12.00,14,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/7b0d3a4923c04dba98703b21ce3a369d.png',1,'2026-04-21 14:05:59','2026-04-21 14:05:59',1,0),(53,14,'安慕希黄桃燕麦酸奶200g*10瓶','1月产伊利安慕希黄桃燕麦酸奶200g*10瓶',30.80,5,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/9f11fcae0ed647189c764765af3e0ec6.png',1,'2026-04-21 15:16:11','2026-04-21 15:23:51',0,0),(55,16,'【晨光】素雅直液式签字笔0.5mm*3支','【晨光】素雅ARP41801直液式全针管中性笔0.5m笔芯签字笔考试办公水笔',11.50,38,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/04874a01393942b1b6b7633029a41927.png',1,'2026-04-21 16:33:05','2026-04-21 16:33:42',1,0),(56,16,'中国风笔记本礼盒','中国风笔记本礼盒定制古风手账本套装日记学生送老师生日毕业礼物',26.80,0,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/eebee0d878f74e6f8fd94d0ae54794e5.png',1,'2026-04-21 16:35:25','2026-04-21 22:46:34',0,0),(57,15,'河南新鲜紫皮脆洋葱3.5斤','河南新鲜紫皮脆洋葱水果红皮洋葱农家自种当季可生吃现挖蔬菜应季',17.84,20,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/238ee843dd334176b8af1aa71f785ed2.png',1,'2026-04-21 16:40:23','2026-04-21 16:40:23',0,0),(58,15,'新鲜意大利生菜3斤','新鲜意大利生菜绿叶菜烤肉涮火锅河南当季罗莎绿生菜低脂蔬菜现挖',10.60,100,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/13bda3f8b435433884e79a5dd26915d0.png',1,'2026-04-21 16:41:53','2026-04-21 16:41:53',0,0),(59,15,'【新鲜紫薯】10斤','【新鲜紫薯】沙地番薯紫心紫薯现挖红薯紫罗兰地瓜蔬菜批发',16.80,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/954cc63558bf4d5388d3c5e90db0d356.png',1,'2026-04-21 16:42:51','2026-04-21 16:42:51',0,0),(60,2,'垃圾桶家用新款太空人款式','垃圾桶家用2025新款客厅卧室卫生间创意厨房可爱高颜值大容量纸篓',46.80,10,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/f8332a44198d421a9b33e6d3a35fdde9.png',1,'2026-04-21 16:46:33','2026-04-21 16:46:33',0,0),(61,2,'衣架家用10个','衣架家用无痕防滑防肩角晾衣挂干湿两用衣服架衣柜收纳神器',8.88,19,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/f1ec0d9791714b129d9587aa2409d4c3.png',1,'2026-04-21 16:47:59','2026-04-21 16:47:59',0,0),(62,2,'旋转拖把','旋转拖把家用一拖净懒人干湿两用免手洗墩布吸水拖布拖地神器套装',44.19,0,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/094170a93ab445348f020edb05aba57a.png',1,'2026-04-21 16:49:07','2026-04-21 23:40:40',0,0),(63,1,'【OPPO】 FindX9Pro 16GB+512GB','【OPPO】 FindX9Pro 哈苏2亿长焦镜头 天玑9500处理器 5G智能手机',5999.00,8,'https://java-poke.oss-cn-beijing.aliyuncs.com/media/d622d78962ff474fb3027c63d9c6256c.png',1,'2026-04-21 17:02:24','2026-04-21 22:39:02',1,0);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_browse_history`
--

DROP TABLE IF EXISTS `product_browse_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_browse_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `viewed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_product` (`user_id`,`product_id`),
  KEY `idx_user_viewed` (`user_id`,`viewed_at`)
) ENGINE=InnoDB AUTO_INCREMENT=192 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品浏览记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_browse_history`
--

LOCK TABLES `product_browse_history` WRITE;
/*!40000 ALTER TABLE `product_browse_history` DISABLE KEYS */;
INSERT INTO `product_browse_history` VALUES (8,2,14,'2026-04-21 18:23:03'),(9,2,15,'2026-04-21 18:23:00'),(10,2,2,'2026-04-22 14:53:16'),(11,2,19,'2026-04-21 10:46:34'),(12,2,20,'2026-04-05 18:27:52'),(13,2,6,'2026-04-21 13:50:39'),(14,2,7,'2026-04-21 16:11:37'),(21,2,13,'2026-04-21 14:10:46'),(22,2,4,'2026-04-22 15:07:34'),(25,2,3,'2026-04-21 16:54:18'),(29,2,9,'2026-04-07 17:07:01'),(30,2,11,'2026-04-19 18:43:54'),(31,2,10,'2026-04-21 18:31:45'),(118,2,8,'2026-04-19 20:08:17'),(121,2,21,'2026-04-21 11:45:24'),(126,2,22,'2026-04-21 11:50:08'),(127,2,23,'2026-04-21 11:46:47'),(133,2,24,'2026-04-21 11:53:14'),(134,2,26,'2026-04-21 22:44:18'),(135,2,27,'2026-04-21 16:12:58'),(136,2,28,'2026-04-21 18:22:14'),(138,2,31,'2026-04-21 16:12:20'),(139,2,32,'2026-04-21 16:12:29'),(140,2,35,'2026-04-21 13:19:03'),(141,2,37,'2026-04-21 13:23:45'),(144,2,12,'2026-04-21 14:06:34'),(148,2,40,'2026-04-21 15:54:08'),(154,2,53,'2026-04-21 16:12:14'),(157,2,25,'2026-04-21 22:51:05'),(160,2,29,'2026-04-21 16:12:55'),(162,2,30,'2026-04-21 16:13:10'),(163,2,49,'2026-04-21 16:27:58'),(165,2,55,'2026-04-21 16:33:51'),(166,2,56,'2026-04-21 18:31:09'),(168,2,62,'2026-04-21 16:49:20'),(172,2,63,'2026-04-22 14:18:03'),(176,2,58,'2026-04-21 18:21:58'),(177,2,59,'2026-04-21 18:22:04'),(187,2,5,'2026-04-22 14:18:42');
/*!40000 ALTER TABLE `product_browse_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_favorite`
--

DROP TABLE IF EXISTS `product_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_product` (`user_id`,`product_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_favorite`
--

LOCK TABLES `product_favorite` WRITE;
/*!40000 ALTER TABLE `product_favorite` DISABLE KEYS */;
INSERT INTO `product_favorite` VALUES (13,2,15,'2026-04-03 18:16:21'),(21,2,2,'2026-04-03 20:24:56'),(22,2,3,'2026-04-03 20:24:58'),(26,2,11,'2026-04-03 20:55:43'),(27,2,7,'2026-04-05 18:33:32'),(28,2,14,'2026-04-07 17:39:51'),(29,2,23,'2026-04-21 11:38:26');
/*!40000 ALTER TABLE `product_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_spec`
--

DROP TABLE IF EXISTS `product_spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_spec` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL COMMENT '逻辑关联 product.id',
  `spec_name` varchar(50) NOT NULL COMMENT '规格名称，如：黑色、白色',
  `sort` tinyint NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `stock` int NOT NULL DEFAULT '0' COMMENT '该规格库存',
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_spec`
--

LOCK TABLES `product_spec` WRITE;
/*!40000 ALTER TABLE `product_spec` DISABLE KEYS */;
INSERT INTO `product_spec` VALUES (40,11,'原味',1,'2026-04-01 11:06:24',13),(41,11,'番茄味',2,'2026-04-01 11:06:24',2),(42,11,'烤肉味',3,'2026-04-01 11:06:24',15),(43,11,'柠檬味',4,'2026-04-01 11:06:24',20),(44,11,'黄瓜味',5,'2026-04-01 11:06:24',18),(53,23,'麻酱味',1,'2026-04-21 03:33:57',33),(54,23,'酸辣味',2,'2026-04-21 03:33:57',33),(55,23,'麻辣味',3,'2026-04-21 03:33:57',33),(58,26,'经典',1,'2026-04-21 04:49:31',50),(59,26,'无糖',2,'2026-04-21 04:49:31',55),(62,13,'红富士',1,'2026-04-21 05:27:02',30),(63,13,'青苹果',2,'2026-04-21 05:27:02',33),(66,52,'葡萄味',0,'2026-04-21 06:05:59',5),(67,52,'水蜜桃味',0,'2026-04-21 06:05:59',9),(68,55,'黑色',0,'2026-04-21 08:33:42',22),(69,55,'红色',1,'2026-04-21 08:33:42',11),(70,55,'蓝色',2,'2026-04-21 08:33:42',5),(71,3,'白色',0,'2026-04-21 08:54:05',2),(72,3,'黑色',0,'2026-04-21 08:54:05',3),(76,15,'玄黑',0,'2026-04-21 08:57:20',9),(77,15,'皓白',0,'2026-04-21 08:57:20',16),(78,15,'瑞红',0,'2026-04-21 08:57:20',3),(82,14,'沪橙风',0,'2026-04-21 08:58:56',6),(83,14,'银色',1,'2026-04-21 08:58:56',2),(84,14,'深蓝色',2,'2026-04-21 08:58:56',3),(85,63,'霜白',0,'2026-04-21 09:02:24',0),(86,63,'追光红',1,'2026-04-21 09:02:24',5),(87,63,'钛色',0,'2026-04-21 09:02:24',3);
/*!40000 ALTER TABLE `product_spec` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promotion`
--

DROP TABLE IF EXISTS `promotion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '活动名称',
  `type` tinyint NOT NULL COMMENT '优惠类型 1=满减 2=折扣',
  `discount_amount` decimal(10,2) DEFAULT NULL COMMENT '满减金额（type=1）',
  `discount_rate` decimal(4,2) DEFAULT NULL COMMENT '折扣率 如0.9=九折（type=2）',
  `min_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最低消费金额，0=无门槛',
  `scope` tinyint NOT NULL COMMENT '范围 1=全场 2=分类 3=单商品',
  `scope_id` bigint DEFAULT NULL COMMENT 'scope=2时category_id，scope=3时product_id',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='限时优惠活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotion`
--

LOCK TABLES `promotion` WRITE;
/*!40000 ALTER TABLE `promotion` DISABLE KEYS */;
INSERT INTO `promotion` VALUES (1,'百亿补贴',2,NULL,0.98,0.00,1,NULL,'2026-04-01 00:00:00','2026-05-01 00:00:00',1,'2026-04-07 14:07:09','2026-04-21 18:19:45'),(2,'苹果国补',1,2000.00,NULL,5000.00,3,14,'2026-04-01 00:00:00','2026-05-20 00:00:00',1,'2026-04-07 14:54:53','2026-04-21 18:22:55'),(3,'电子产品大促销',2,NULL,0.90,1000.00,2,1,'2026-04-03 00:00:00','2026-05-12 00:00:00',1,'2026-04-07 15:35:09','2026-04-21 18:23:47'),(4,'电子产品促销',1,50.00,NULL,1000.00,2,1,'2026-04-08 00:00:00','2026-04-15 00:00:00',1,'2026-04-09 14:06:46','2026-04-15 14:01:12'),(5,'饮料促销',2,NULL,0.90,0.00,2,6,'2026-04-14 00:00:00','2026-05-31 00:00:00',1,'2026-04-21 18:20:39','2026-04-21 18:20:39');
/*!40000 ALTER TABLE `promotion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return_order`
--

DROP TABLE IF EXISTS `return_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `reason` varchar(255) NOT NULL COMMENT '退货原因',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0申请中 1已批准 2已拒绝 3退款处理中 4已退款 5已取消',
  `expected_refund_amount` decimal(10,2) NOT NULL COMMENT '期望退款金额',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `images` varchar(1000) DEFAULT NULL COMMENT '退货凭证图片，多张逗号分隔',
  `order_item_id` bigint NOT NULL COMMENT '退货订单项ID',
  `actual_refund_amount` decimal(10,2) DEFAULT NULL COMMENT '实际退款金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return_order`
--

LOCK TABLES `return_order` WRITE;
/*!40000 ALTER TABLE `return_order` DISABLE KEYS */;
INSERT INTO `return_order` VALUES (2,28,2,'不好喝',1,99.00,'','2026-03-31 18:34:23','2026-03-31 18:34:23','https://java-poke.oss-cn-beijing.aliyuncs.com/media/8ec3937f295244a0979ecac118944efa.jpg,https://java-poke.oss-cn-beijing.aliyuncs.com/media/2e2c883cae574338a11314518b9c011e.png',42,99.00),(3,33,2,'不想要了',2,10.00,'?','2026-04-01 10:41:36','2026-04-01 10:41:36','https://java-poke.oss-cn-beijing.aliyuncs.com/media/408f6b03928d4a4f87f0446f7d1d77d9.jpg',50,10.00),(4,35,2,'111',4,5.00,NULL,'2026-04-01 10:51:57','2026-04-01 10:51:57','https://java-poke.oss-cn-beijing.aliyuncs.com/media/408f6b03928d4a4f87f0446f7d1d77d9.jpg',52,5.00),(5,36,2,'34',4,5.00,NULL,'2026-04-01 10:55:45','2026-04-01 10:55:45','https://java-poke.oss-cn-beijing.aliyuncs.com/media/408f6b03928d4a4f87f0446f7d1d77d9.jpg',53,3.00),(6,30,2,'bnbb',5,1.00,NULL,'2026-04-01 13:24:44','2026-04-01 13:24:44','',45,NULL),(7,28,2,'eewe',5,1.00,NULL,'2026-04-01 13:25:00','2026-04-01 13:25:00','',43,NULL),(8,24,2,'213',1,1.00,NULL,'2026-04-01 13:25:53','2026-04-01 13:25:53','',37,1.00),(9,30,2,'t5',2,10.00,'edrfggfred','2026-04-01 13:54:13','2026-04-01 13:54:13','',46,NULL),(13,27,2,'是非观',1,1.00,NULL,'2026-04-02 19:35:29','2026-04-02 19:35:29','',41,1.00),(16,25,2,'放的歌',1,1.00,NULL,'2026-04-03 14:02:53','2026-04-07 14:29:48','',38,1.00),(17,48,2,'1',5,10399.00,NULL,'2026-04-10 19:27:58','2026-04-10 22:39:08','',72,NULL),(18,57,2,'1',5,6666.00,NULL,'2026-04-20 21:51:29','2026-04-20 22:00:55','',81,NULL);
/*!40000 ALTER TABLE `return_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '评价人，逻辑关联 user.id',
  `order_item_id` bigint NOT NULL COMMENT '订单项，逻辑关联 order_item.id',
  `product_id` bigint NOT NULL COMMENT '商品，逻辑关联 product.id',
  `rating` tinyint NOT NULL COMMENT '评分 1-5',
  `content` text NOT NULL COMMENT '评价内容',
  `media_urls` json DEFAULT NULL COMMENT '图片/视频链接数组',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_item` (`order_item_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (1,2,37,2,5,'非常好耳机','[\"https://java-poke.oss-cn-beijing.aliyuncs.com/media/5f3be311f6bf47e29f338285395a19b5.jpg\"]','2026-03-29 05:07:45','2026-03-29 05:07:45'),(3,2,39,4,5,'jjjhoi','[\"https://java-poke.oss-cn-beijing.aliyuncs.com/media/5250ccc55b0b493cb6a83d012446d213.png\", \"https://java-poke.oss-cn-beijing.aliyuncs.com/media/8210d68f5afc45e6a36b6537e51325f4.jpg\"]','2026-03-29 09:21:05','2026-03-29 09:21:05'),(5,2,43,2,5,'非常好',NULL,'2026-03-31 11:07:48','2026-03-31 11:07:48'),(6,2,53,11,5,'真的夯爆了','[\"https://java-poke.oss-cn-beijing.aliyuncs.com/media/251f399d8c4b471eb472821ebef41878.png\"]','2026-04-01 12:07:29','2026-04-01 12:07:29'),(7,5,49,11,2,'拉完了',NULL,'2026-04-01 12:11:36','2026-04-01 12:11:36'),(8,2,66,11,5,'4.5一包要什么自行车',NULL,'2026-04-08 02:23:00','2026-04-08 02:23:00'),(9,2,153,49,5,'重生之我是巴菲特',NULL,'2026-04-21 08:28:54','2026-04-21 08:28:54');
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review_comment`
--

DROP TABLE IF EXISTS `review_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_id` bigint NOT NULL COMMENT '所属评价，逻辑关联 review.id',
  `user_id` bigint NOT NULL COMMENT '评论人，逻辑关联 user.id',
  `parent_id` bigint DEFAULT NULL COMMENT '回复的评论，逻辑关联 review_comment.id，NULL 表示一级评论',
  `content` text NOT NULL COMMENT '评论内容',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_review` (`review_id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review_comment`
--

LOCK TABLES `review_comment` WRITE;
/*!40000 ALTER TABLE `review_comment` DISABLE KEYS */;
INSERT INTO `review_comment` VALUES (3,1,2,NULL,'不好吗?','2026-03-29 05:08:31'),(8,6,2,NULL,'一般般','2026-04-01 12:07:44'),(9,6,2,NULL,'怎么可能','2026-04-01 12:07:53'),(10,6,2,8,'@Eric ？','2026-04-01 12:08:03'),(11,6,2,8,'@Eric ？','2026-04-01 12:08:09'),(12,6,2,8,'@Eric ？','2026-04-01 12:08:14'),(13,6,2,NULL,'对方过后','2026-04-01 12:08:19'),(14,6,2,NULL,'嗯挺听话','2026-04-01 12:08:22'),(15,6,2,NULL,'天天打个饭盒给','2026-04-01 12:08:25'),(16,6,5,NULL,'我看行','2026-04-01 12:09:37'),(17,7,2,NULL,'拉在哪???','2026-04-01 12:13:11');
/*!40000 ALTER TABLE `review_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review_like`
--

DROP TABLE IF EXISTS `review_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_id` bigint NOT NULL COMMENT '被点赞的评价，逻辑关联 review.id',
  `user_id` bigint NOT NULL COMMENT '点赞人，逻辑关联 user.id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_user` (`review_id`,`user_id`),
  KEY `idx_review` (`review_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价点赞';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review_like`
--

LOCK TABLES `review_like` WRITE;
/*!40000 ALTER TABLE `review_like` DISABLE KEYS */;
INSERT INTO `review_like` VALUES (7,1,2,'2026-03-29 10:06:22'),(8,2,2,'2026-03-30 10:35:01'),(9,5,2,'2026-03-31 11:12:58'),(10,6,2,'2026-04-01 12:07:45'),(11,6,5,'2026-04-01 12:12:05'),(12,9,2,'2026-04-21 08:28:58');
/*!40000 ALTER TABLE `review_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seckill_activity`
--

DROP TABLE IF EXISTS `seckill_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seckill_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '活动名称',
  `activity_type` tinyint DEFAULT NULL COMMENT '活动种类,1=秒杀商品活动,2=秒杀订单券活动',
  `description` varchar(500) DEFAULT NULL COMMENT '活动描述',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 2=禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seckill_activity`
--

LOCK TABLES `seckill_activity` WRITE;
/*!40000 ALTER TABLE `seckill_activity` DISABLE KEYS */;
INSERT INTO `seckill_activity` VALUES (1,'秒杀测试',2,'12','2026-04-01 00:00:00','2026-04-30 00:00:00',1,'2026-04-13 20:36:01','2026-04-14 19:56:02'),(2,'商品秒杀活动',1,'1','2026-04-01 00:00:00','2026-04-30 00:00:00',1,'2026-04-14 20:13:46','2026-04-14 20:13:46'),(3,'商品大促销',1,'111','2026-04-19 00:00:00','2026-05-30 00:00:00',1,'2026-04-20 22:37:04','2026-04-20 22:37:04'),(4,'水果秒杀',1,'水果秒杀','2026-04-01 00:00:00','2026-05-28 00:00:00',1,'2026-04-21 18:33:02','2026-04-21 18:33:02'),(5,'618秒杀',1,'','2026-04-21 00:00:00','2026-06-18 00:00:00',1,'2026-04-21 22:36:58','2026-04-21 22:36:58'),(6,'清凉夏日',1,'','2026-04-21 00:00:00','2026-06-30 00:00:00',1,'2026-04-21 22:40:06','2026-04-21 22:40:06'),(7,'学生补贴秒杀活动',1,'','2026-04-07 00:00:00','2027-04-07 00:00:00',1,'2026-04-21 22:46:01','2026-04-21 22:46:01'),(8,'零食秒杀',1,'','2026-04-21 00:00:00','2026-05-05 00:00:00',1,'2026-04-21 22:48:21','2026-04-21 22:48:21'),(9,'劳动节秒杀',1,'','2026-04-23 00:00:00','2026-05-16 23:16:35',1,'2026-04-21 23:16:54','2026-04-21 23:16:54');
/*!40000 ALTER TABLE `seckill_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seckill_item`
--

DROP TABLE IF EXISTS `seckill_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seckill_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint DEFAULT NULL COMMENT '关联活动',
  `item_type` tinyint NOT NULL COMMENT '1=商品 2=订单券',
  `product_id` bigint DEFAULT NULL COMMENT '关联商品(item_type=1)',
  `product_spec_id` bigint DEFAULT NULL,
  `coupon_id` bigint DEFAULT NULL COMMENT '关联优惠券(item_type=2)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=上架 2=下架',
  `seckill_price` decimal(10,2) DEFAULT NULL COMMENT '秒杀价(item_type=1)',
  `seckill_stock` int DEFAULT NULL COMMENT '秒杀库存',
  `per_limit` int NOT NULL DEFAULT '1' COMMENT '每人限购数',
  `sold_count` int DEFAULT '0' COMMENT '已售数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seckill_item`
--

LOCK TABLES `seckill_item` WRITE;
/*!40000 ALTER TABLE `seckill_item` DISABLE KEYS */;
INSERT INTO `seckill_item` VALUES (4,1,2,NULL,NULL,15,1,NULL,NULL,1,0,'2026-04-14 10:54:16','2026-04-15 21:42:24'),(5,2,1,14,NULL,NULL,1,6666.00,86,1,1,'2026-04-14 10:55:23','2026-04-20 16:14:27'),(7,1,2,NULL,NULL,18,1,NULL,NULL,1,0,'2026-04-15 13:21:20','2026-04-15 21:42:24'),(8,1,2,NULL,NULL,16,1,NULL,NULL,1,0,'2026-04-15 13:21:30','2026-04-15 21:42:24'),(9,1,2,NULL,NULL,19,1,NULL,NULL,1,0,'2026-04-15 20:18:42','2026-04-15 20:18:52'),(10,1,2,NULL,NULL,21,1,NULL,NULL,1,0,'2026-04-15 21:08:56','2026-04-15 21:09:02'),(11,1,2,NULL,NULL,22,1,NULL,NULL,1,0,'2026-04-19 21:11:56','2026-04-19 21:12:00'),(12,2,1,11,NULL,NULL,1,3.00,33,2,0,'2026-04-19 21:35:07','2026-04-20 16:15:04'),(13,2,1,2,NULL,NULL,1,99.00,100,1,0,'2026-04-19 21:42:02','2026-04-21 17:48:34'),(14,NULL,1,3,NULL,NULL,2,199.00,10,3,0,'2026-04-19 21:52:14','2026-04-21 17:51:21'),(15,2,1,5,NULL,NULL,1,10.00,30,3,0,'2026-04-19 21:53:19','2026-04-20 16:15:14'),(16,3,1,7,NULL,NULL,1,5.00,33,3,0,'2026-04-20 22:37:45','2026-04-20 22:37:51'),(17,4,1,36,NULL,NULL,1,9.90,3,1,0,'2026-04-21 18:37:47','2026-04-21 18:37:54'),(19,5,1,63,85,NULL,1,5555.00,3,1,0,'2026-04-21 19:20:51','2026-04-21 23:11:22'),(20,9,1,62,NULL,NULL,1,30.00,5,1,0,'2026-04-21 19:21:18','2026-04-21 23:40:40'),(21,6,1,26,58,NULL,1,9.90,5,1,0,'2026-04-21 22:40:34','2026-04-21 23:11:11'),(22,7,1,56,NULL,NULL,1,19.90,5,1,0,'2026-04-21 22:46:25','2026-04-21 22:46:34'),(23,8,1,25,NULL,NULL,1,9.90,5,1,0,'2026-04-21 22:48:49','2026-04-21 23:10:56');
/*!40000 ALTER TABLE `seckill_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像OSS地址',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色: 0=用户 1=管理员 2=boss',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0=禁用 1=正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `profile_signature` varchar(33) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个性签名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'boss','$2a$10$/RSqbxOPtklntbX7bi9Hle0dIUmLtpGtVtMpvM6Y56PBu68FUKCby',NULL,'18595222222','https://java-poke.oss-cn-beijing.aliyuncs.com/images/0117b3232afe4ba1a58355b9cf140550.jpg',2,1,'2026-03-26 12:12:15','2026-04-22 15:26:19',NULL),(2,'Eric','$2a$10$F5Uh7PmJgWSBGA5qn5OkQepo5k4V7/XrbDlj9Q3G9eO3cDXdDqcES',NULL,'19863422222','https://java-poke.oss-cn-beijing.aliyuncs.com/images/0f48d225ddd54322bd4364d2472e56f3.jpg',0,1,'2026-03-26 17:19:47','2026-04-22 15:27:23','嘿嘿'),(3,'admin','$2a$10$itOiOKbnxNft6Duav0hXxO3mYZVPJvKqqUCXuisuqDx03goj/hi5W',NULL,'18595555555','https://java-poke.oss-cn-beijing.aliyuncs.com/media/0f8a2fd7d66548178b934d8ea36d5a57.png',1,1,'2026-03-28 15:24:15','2026-04-22 15:31:22',NULL),(5,'user','$2a$10$jFgc0jyfzRH5m7L0Hq7Rbu0On6/JEf81nfTWITBKvMNnDbHDzloZa',NULL,'18590009000',NULL,0,1,'2026-03-29 11:14:26','2026-04-22 15:31:34','1');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_checkin`
--

DROP TABLE IF EXISTS `user_checkin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_checkin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `checkin_date` date NOT NULL COMMENT '签到日期',
  `points_earned` int NOT NULL COMMENT '本次获得积分',
  `consecutive_days` int NOT NULL COMMENT '签到时的连续天数快照',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`checkin_date`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户签到记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_checkin`
--

LOCK TABLES `user_checkin` WRITE;
/*!40000 ALTER TABLE `user_checkin` DISABLE KEYS */;
INSERT INTO `user_checkin` VALUES (1,2,'2026-04-11',10,1,'2026-04-11 21:31:18'),(2,2,'2026-04-12',10,2,'2026-04-12 16:51:05'),(3,2,'2026-04-15',10,1,'2026-04-15 19:44:07'),(4,2,'2026-04-16',11,2,'2026-04-16 09:38:36'),(5,2,'2026-04-17',11,3,'2026-04-17 19:18:25'),(6,5,'2026-04-17',11,1,'2026-04-17 19:54:14'),(7,2,'2026-04-18',11,4,'2026-04-18 10:37:24'),(8,5,'2026-04-18',11,2,'2026-04-18 10:40:00'),(9,2,'2026-04-19',11,5,'2026-04-19 11:10:29'),(10,5,'2026-04-19',15,3,'2026-04-19 11:16:30'),(11,5,'2026-04-20',11,4,'2026-04-20 15:59:57'),(12,2,'2026-04-21',11,1,'2026-04-21 14:07:44');
/*!40000 ALTER TABLE `user_checkin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_points`
--

DROP TABLE IF EXISTS `user_points`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_points` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `total_points` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户积分账户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_points`
--

LOCK TABLES `user_points` WRITE;
/*!40000 ALTER TABLE `user_points` DISABLE KEYS */;
INSERT INTO `user_points` VALUES (1,2,70,'2026-04-11 21:31:11','2026-04-21 14:07:39'),(6,5,48,'2026-04-17 19:53:59','2026-04-20 15:59:48');
/*!40000 ALTER TABLE `user_points` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_points_log`
--

DROP TABLE IF EXISTS `user_points_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_points_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `change_type` tinyint NOT NULL COMMENT '1=签到 2=兑换消费',
  `points_delta` int NOT NULL COMMENT '正=增加 负=减少',
  `balance` int NOT NULL COMMENT '变动后余额快照',
  `remark` varchar(100) DEFAULT NULL,
  `ref_id` bigint DEFAULT NULL COMMENT '关联业务ID（预留）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_points_log`
--

LOCK TABLES `user_points_log` WRITE;
/*!40000 ALTER TABLE `user_points_log` DISABLE KEYS */;
INSERT INTO `user_points_log` VALUES (1,2,1,10,10,'普通签到',NULL,'2026-04-11 21:31:18'),(2,2,1,10,20,'普通签到',NULL,'2026-04-12 16:51:05'),(3,2,2,-5,15,'兑换券：3元无门槛积分订单券',2,'2026-04-12 17:41:44'),(4,2,2,-10,5,'兑换券：95折积分订单券',3,'2026-04-12 19:14:22'),(5,2,1,10,15,'普通签到',NULL,'2026-04-15 19:44:07'),(6,2,1,11,26,'普通签到',NULL,'2026-04-16 09:38:36'),(7,2,1,11,37,'普通签到',NULL,'2026-04-17 19:18:25'),(8,5,1,11,11,'普通签到',NULL,'2026-04-17 19:54:14'),(9,2,1,11,48,'普通签到',NULL,'2026-04-18 10:37:24'),(10,5,1,11,22,'普通签到',NULL,'2026-04-18 10:40:00'),(11,2,1,11,59,'普通签到',NULL,'2026-04-19 11:10:29'),(12,5,1,15,37,'连续签到3天，额外奖励！',NULL,'2026-04-19 11:16:30'),(13,5,1,11,48,'普通签到',NULL,'2026-04-20 15:59:57'),(14,2,1,11,70,'普通签到',NULL,'2026-04-21 14:07:44');
/*!40000 ALTER TABLE `user_points_log` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-22 15:41:38
