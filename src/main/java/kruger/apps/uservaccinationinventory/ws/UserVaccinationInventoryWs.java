package kruger.apps.uservaccinationinventory.ws;


public class UserVaccinationInventoryWs {
	@RestController
	@RequestMapping("/v1/pay")
	public class PayWs {

		@Value("${email.subepagos}")
		private String emailSubePagos;

		@Value("${email.wireTransfer}")
		private String emailWireTransfer;

		@Value("${email.subecharge}")
		private String emailSubeCharge;

		public static final Logger LOGGER = LoggerFactory.getLogger(PayWs.class);

		@Autowired
		private QRCodeService qrCodeService;

		@Autowired
		private TransactionsService transactionsService;

		@Autowired
		private WalletService walletService;

		@Autowired
		private LimitService limitService;

		@Autowired
		private OperationService operationService;

		@Autowired
		private UserDao userDao;

		@RequestMapping(value = "/createWallet", method = RequestMethod.PUT, produces = "application/json")
		@ApiOperation(value = "Crear nueva billetera asociadas a un usuario")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = String.class), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
				@ApiResponse(code = 400, message = "Bad Request", response = String.class)
		})
		public @ResponseBody ResponseEntity<?> createWallet(@RequestBody CreateWalletRequest saveCardRequest, @CurrentUser UserDetails userDetail){

			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");

			try{
				UserInfo user = userDao.findOneByEmail(saveCardRequest.getUserEmail());

				Wallet wallet = walletService.getWalletByUserId(user.getUserId());

				if(wallet != null){
					apiErrorResponse.getFieldsErrors().put("wallet", WALLET_EXISTS);
					return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
				}

				walletService.create(user);

			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError receivedApiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);
				receivedApiError.getFieldsErrors().entrySet().stream().forEach(err -> apiErrorResponse.getFieldsErrors().put(err.getKey(), err.getValue()));
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);

			} catch(ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return new ResponseEntity<>("Se creo la cuenta Sube correctamente", HttpStatus.OK);
		}

		@RequestMapping(value = "/sendMoney", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Envia dinero al destinatario por billetera")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> sendMoneyByEmail(@RequestBody SendMoneyRequest sendMoneyRequest, @CurrentUser UserDetails user){

			TransactionType codeTrasactionType = transactionsService.getTransactionTypeCode(1L);
			StringBuilder errorMessages = new StringBuilder();

			try{

				if(sendMoneyRequest.getTransactionAmount().compareTo(new BigDecimal(0L)) < 1){
					errorMessages.append(INVALID_AMOUNT).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				UserInfo userInfo = userDao.findOneByEmail(sendMoneyRequest.getUserEmailDestination());

				if(userInfo.getUserId().equals(user.getUserId())){
					errorMessages.append(SAME_WALLET).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				Wallet destinationWallet = walletService.getWalletByUserId(userInfo.getUserId());
				Wallet sourceWallet = walletService.getWalletByUserId(user.getUserId());

				if(destinationWallet == null){
					errorMessages.append(DESTINATION_WALLET_NOT_EXISTS).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				if(sourceWallet == null){
					errorMessages.append(SOURCE_WALLET_NOT_EXISTS).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				DataValidate dataValidate = new DataValidate(destinationWallet, sourceWallet, sendMoneyRequest.getTransactionAmount(), errorMessages, codeTrasactionType);
				limitService.validateLimits(dataValidate);

				if(dataValidate.getErrorMessages().length() == 0){
					operationService.operate(sourceWallet, destinationWallet, sendMoneyRequest.getTransactionAmount(), codeTrasactionType.getTypeCode(), null, user.getUserId());
				} else{
					return new ResponseEntity<>(dataValidate.getErrorMessages(), HttpStatus.BAD_REQUEST);
				}

			} catch(ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError apiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);

				if(apiError.getFieldsErrors() != null && !apiError.getFieldsErrors().isEmpty()){
					apiError.getFieldsErrors().entrySet().stream().forEach(err -> errorMessages.append(err.getValue()).append("\n"));
				}

				errorMessages.append(apiError.getMessage());
				return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return new ResponseEntity<>("Dinero enviado correctamente", HttpStatus.OK);

		}

		@RequestMapping(value = "/getBalance", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Consulta de saldo por usuario", response = BalanceResponse.class)
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = BalanceResponse.class), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> getBalance(@CurrentUser UserDetails userDetail){

			StringBuilder errorMessages = new StringBuilder();
			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");

			try{
				UserInfo user = userDao.findOneByEmail(userDetail.getEmail());

				Wallet wallet = walletService.getWalletByUserId(user.getUserId());

				if(wallet == null){
					apiErrorResponse.getFieldsErrors().put("wallet", USER_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
				}

				BalanceResponse response = new BalanceResponse(walletService.getBalanceByUserId(user.getUserId()));
				return new ResponseEntity<>(response, HttpStatus.OK);

			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError apiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);
				apiError.getFieldsErrors().entrySet().stream().forEach(err -> errorMessages.append(err.getValue()).append("\n"));
				return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@RequestMapping(value = "/getTransactions", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Obtiene el historial paginado de transacciones", response = TransactionDetailsResponse.class, responseContainer = "List")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = TransactionDetailsResponse.class, responseContainer = "List"),
				@ApiResponse(code = 500, message = "Internal Server Error", response = String.class), @ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> getTransactions(@RequestBody(required = false) GetTransactionsRequest request, @CurrentUser UserDetails userDetails){
			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");

			try{
				UserInfo userInfo = userDao.findOneByEmail(userDetails.getEmail());
				Wallet userWallet = walletService.getWalletByUserId(userInfo.getUserId());

				if(userWallet == null){
					apiErrorResponse.getFieldsErrors().put("sourceWallet", SOURCE_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
				}

				Date startDate = request.getStartDate();
				Date endDate = request.getEndDate();

				if(startDate == null){
					startDate = new Date(0);
				} else{ // para que la fecha sea tenida en cuenta por .after() que es 'mayor estricto'
						// en vez de 'mayor e igual'
					startDate = new Date(startDate.getTime() - 1);
				}

				if(endDate == null){
					endDate = new Date();
				} else{ // para que la fecha sea tenida en cuenta por .after() que es 'mayor estricto'
						// en vez de 'mayor e igual'
					endDate = new Date(endDate.getTime() + 1);
				}

				List<TransactionDetailsResponse> transactionDetailsResponse = transactionsService.getTransactionsByWalletAndDates(userWallet, startDate, endDate, PageRequest
					.of(request.getPageNumber() == null ? 0 : request.getPageNumber(), request.getPageSize() == null ? Integer.MAX_VALUE : request.getPageSize(), Direction.DESC, "dateTransactionDetail"));

				return new ResponseEntity<>(transactionDetailsResponse, HttpStatus.OK);
			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError receivedApiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);
				receivedApiError.getFieldsErrors().entrySet().stream().forEach(err -> apiErrorResponse.getFieldsErrors().put(err.getKey(), err.getValue()));
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@RequestMapping(value = "/getTransactionsByUserId", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Obtiene el historial paginado de transacciones", response = TransactionDetailsResponse.class, responseContainer = "List")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = TransactionDetailsResponse.class, responseContainer = "List"),
				@ApiResponse(code = 500, message = "Internal Server Error", response = String.class), @ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> getTransactionsByUserId(@RequestBody GetTransactionByUserIdRequest request){

			ApiErrorv2 apiError = new ApiErrorv2();

			try{

				UserInfo userInfo = userDao.findOneByEmail(request.getUserEmail());
				Wallet userWallet = walletService.getWalletByUserId(userInfo.getUserId());

				if(userWallet == null){
					apiError.setTitle("Error en parametros");
					apiError.setType("Parametro");
					apiError.getDetail().put("walletId", SOURCE_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
				}

				List<TransactionDetailsResponse> transactionDetailsResponse = transactionsService.getTransactionsByWalletAndDates(userWallet, request.getStartDate(), request.getEndDate(), PageRequest
					.of(request.getPageNumber() == null ? 0 : request.getPageNumber(), request.getPageSize() == null ? Integer.MAX_VALUE : request.getPageSize(), Direction.ASC, "dateTransactionDetail"));

				return new ResponseEntity<>(transactionDetailsResponse, HttpStatus.OK);
			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError apiErrorReceived = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);
				StringBuilder errorMessages = new StringBuilder();

				if(apiErrorReceived.getFieldsErrors() != null && !apiErrorReceived.getFieldsErrors().isEmpty()){
					apiErrorReceived.getFieldsErrors().entrySet().stream().forEach(err -> errorMessages.append(err.getValue()).append("\n"));
				}
				return new ResponseEntity<>(apiErrorReceived, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				apiError.setTitle("Error Interno");
				apiError.setType("Error: Exception");
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@RequestMapping(value = "/getLatestRecipients", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Obtiene el historial paginado de los ultimos usuarios a los que se envió dinero", response = LatestRecipientsResponse.class, responseContainer = "List")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = LatestRecipientsResponse.class, responseContainer = "List"),
				@ApiResponse(code = 500, message = "Internal Server Error", response = String.class), @ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})

		public @ResponseBody ResponseEntity<?> getLatestRecipients(@RequestBody GetLatestRecipientsRequest request, @CurrentUser UserDetails userDetails){

			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");

			try{

				UserInfo userInfo = userDao.findOneByEmail(userDetails.getEmail());
				Wallet userWallet = walletService.getWalletByUserId(userInfo.getUserId());

				if(userWallet == null){
					apiErrorResponse.getFieldsErrors().put("sourceWallet", SOURCE_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
				}

				List<LatestRecipientsResponse> latestRecipientsResponse = transactionsService.getLatestRecipients(userWallet, request.getPageNumber() == null ? 0 : request.getPageNumber(),
					request.getPageSize() == null ? Integer.MAX_VALUE : request.getPageSize());

				return new ResponseEntity<>(latestRecipientsResponse, HttpStatus.OK);

			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError receivedApiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);
				receivedApiError.getFieldsErrors().entrySet().stream().forEach(err -> apiErrorResponse.getFieldsErrors().put(err.getKey(), err.getValue()));
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@RequestMapping(value = "/generateDynamicMerchantQRCode", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Genera un codigo QR dinamico y devuelve la string base64 de la imagen PNG correspondiente")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Base64QRResponse.class), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})

		public @ResponseBody ResponseEntity<?> generateDynamicMerchantQRCode(@RequestBody(required = false) GenerateQRCodeRequest request, @CurrentUser UserDetails userDetails){

			String imageBase64;
			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");
			Base64QRResponse response = new Base64QRResponse();

			try{
				UserInfo userInfo = userDao.findOneByEmail(userDetails.getEmail());
				Wallet userWallet = walletService.getWalletByUserId(userInfo.getUserId());

				if(userWallet == null){
					apiErrorResponse.getFieldsErrors().put("sourceWallet", SOURCE_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
				}

				imageBase64 = qrCodeService.generateBase64QRCode(new DataInputQr(userInfo, userWallet.getIdWallet(), request));
				response.setQrCodeBase64(imageBase64);
			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError receivedApiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);
				receivedApiError.getFieldsErrors().entrySet().stream().forEach(err -> apiErrorResponse.getFieldsErrors().put(err.getKey(), err.getValue()));
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		@RequestMapping(value = "/generateDynamicQRCodePNG", method = RequestMethod.POST, produces = MediaType.IMAGE_PNG_VALUE)
		@ApiOperation(value = "Genera un codigo QR dinamigo y devuelve la imagen PNG correspondiente", response = QRCodeResponse.class)
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = QRCodeResponse.class), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})

		public @ResponseBody ResponseEntity<?> generateDynamicMerchantQRCodePNG(@RequestBody(required = false) GenerateQRCodeRequest request, @CurrentUser UserDetails userDetails){

			QRCodeResponse response;
			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");

			try{
				UserInfo userInfo = userDao.findOneByEmail(userDetails.getEmail());
				Wallet userWallet = walletService.getWalletByUserId(userInfo.getUserId());

				if(userWallet == null){
					apiErrorResponse.getFieldsErrors().put("sourceWallet", SOURCE_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
				}

				response = qrCodeService.generateQRCode(new DataInputQr(userInfo, userWallet.getIdWallet(), request));

			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError receivedApiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);
				receivedApiError.getFieldsErrors().entrySet().stream().forEach(err -> apiErrorResponse.getFieldsErrors().put(err.getKey(), err.getValue()));
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return new ResponseEntity<>(response.getQrCode(), HttpStatus.OK);
		}

		@RequestMapping(value = "/validateScannedQRCode", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Devuelve si la String recibida corresponde a un QR de pago y su identificador de billetera", response = DataInputQr.class)
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DataInputQr.class), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> validateScannedQRCode(@RequestBody(required = false) ValidateScannedQRCodeRequest request){

			DataInputQr response;
			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");

			try{
				response = qrCodeService.validateQRCodeString(request.getScannedQRString());
			} catch(RuntimeException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				apiErrorResponse.getFieldsErrors().put("scannedQRString", INVALID_QR_CODE);
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		@RequestMapping(value = "/QrPay", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Paga dinero al destinatario del QR")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error"),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> payMoney(@RequestBody DataInputQrRequest dataInputQr, @CurrentUser UserDetails user){

			TransactionType codeTrasactionType = transactionsService.getTransactionTypeCode(2L);
			StringBuilder errorMessages = new StringBuilder();

			try{

				Wallet destinationWallet = walletService.getWalletByWallet(dataInputQr.getDestinationWallet());
				Wallet sourceWallet = walletService.getWalletByUserId(user.getUserId());

				if(destinationWallet == null){
					errorMessages.append(DESTINATION_WALLET_NOT_EXISTS).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				if(sourceWallet == null){
					errorMessages.append(SOURCE_WALLET_NOT_EXISTS).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				if(destinationWallet.getIdWallet() == sourceWallet.getIdWallet()){
					errorMessages.append(SAME_WALLET).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				DataValidate dataValidate = new DataValidate(destinationWallet, sourceWallet, dataInputQr.getAmount(), errorMessages, codeTrasactionType);
				limitService.validateLimits(dataValidate);

				if(dataValidate.getErrorMessages().length() == 0){
					operationService.operate(sourceWallet, destinationWallet, dataInputQr.getAmount(), codeTrasactionType.getTypeCode(), null, user.getUserId());
				} else{
					return new ResponseEntity<>(dataValidate.getErrorMessages(), HttpStatus.BAD_REQUEST);
				}
			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError apiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);
				if(apiError.getFieldsErrors() != null && !apiError.getFieldsErrors().isEmpty()){
					apiError.getFieldsErrors().entrySet().stream().forEach(err -> errorMessages.append(err.getValue()).append("\n"));
				}
				errorMessages.append(apiError.getMessage());
				return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
			}

			catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return new ResponseEntity<>("Pago realizado correctamente", HttpStatus.OK);
		}

		@RequestMapping(value = "/subeChargePay", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Utilizado por servicio Sube-Charge. Realiza el pago de la carga a realizar")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error"),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> subeChargePay(@RequestBody SubeChargeRequest subeChargeRequest){

			TransactionType codeTrasactionType = transactionsService.getTransactionTypeCode(3L);
			StringBuilder errorMessages = new StringBuilder();

			try{

				UserInfo userSubeCharge = userDao.findOneByEmail(emailSubeCharge);

				Wallet destinationWallet = walletService.getWalletByUserId(userSubeCharge.getUserId());
				Wallet sourceWallet = walletService.getWalletByUserId(subeChargeRequest.getUserId());

				if(destinationWallet == null){
					errorMessages.append(DESTINATION_WALLET_NOT_EXISTS).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				if(sourceWallet == null){
					errorMessages.append(SOURCE_WALLET_NOT_EXISTS).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				if(destinationWallet.getIdWallet() == sourceWallet.getIdWallet()){
					errorMessages.append(SAME_WALLET).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				DataValidate dataValidate = new DataValidate(destinationWallet, sourceWallet, subeChargeRequest.getAmount(), errorMessages, codeTrasactionType);
				limitService.validateLimits(dataValidate);

				if(dataValidate.getErrorMessages().length() == 0){

					Transaction transaction = operationService.operate(sourceWallet, destinationWallet, subeChargeRequest.getAmount(), codeTrasactionType.getTypeCode(), subeChargeRequest.getChargeId(),
						subeChargeRequest.getUserId());
					return new ResponseEntity<>(transaction.getIdTransaction(), HttpStatus.OK);

				} else{
					return new ResponseEntity<>(dataValidate.getErrorMessages(), HttpStatus.BAD_REQUEST);
				}

			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError apiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);
				if(apiError.getFieldsErrors() != null && !apiError.getFieldsErrors().isEmpty()){
					apiError.getFieldsErrors().entrySet().stream().forEach(err -> errorMessages.append(err.getValue()).append("\n"));
				}
				errorMessages.append(apiError.getMessage());
				return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
			} catch(ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}

		@RequestMapping(value = "/reverseTransaction", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Reversa de operacion realizada previamente.")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error"),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> reverseTransaction(@RequestBody ReverseOperationRequest dataInputQr){

			TransactionType reverseType = transactionsService.getTransactionTypeCode(5L);
			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");
			Optional<Transaction> transaction = transactionsService.findOne(dataInputQr.getTransactionId());

			if(!transaction.isPresent()){
				apiErrorResponse.getFieldsErrors().put("transactionId", TRANSACTION_NOT_EXIST);
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			}

			if(transaction.get().getType().equals(reverseType)){
				apiErrorResponse.getFieldsErrors().put("transactionId", TRANSACTION_IS_REVERSE);
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			}

			if(transactionsService.findByReversedTransactionId(transaction.get().getIdTransaction()).isPresent()){
				apiErrorResponse.getFieldsErrors().put("transactionId", TRANSACTION_REVERSED);
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			}

			try{
				Long requestingUserId = null;
				// detalle de la transaccion que indica el origen de la carga
				List<TransactionDetails> listTransactionDetails = transaction.get().getTransactionDetails().stream().filter(dt -> dt.isCredit() == false).collect(Collectors.toList());
				if(!listTransactionDetails.isEmpty()){
					requestingUserId = listTransactionDetails.get(0).getWallet().getUsersWallets().get(0).getUserId();
					LOGGER.info("userID: " + requestingUserId);
				}

				Transaction reversedTransaction = operationService.reversate(transaction.get(), reverseType, requestingUserId);
				return new ResponseEntity<>(reversedTransaction.getIdTransaction(), HttpStatus.OK);

			} catch(ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@RequestMapping(value = "/getTransactionReversed/{transactionId}", method = RequestMethod.GET, produces = "application/json")
		@ApiOperation(value = "Consulta si la transaccion fue reversada. Devuelve los datos de la reversa.")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error"),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> getTransactionReversed(@PathVariable("transactionId") Long transactionId){

			TransactionType reverseType = transactionsService.getTransactionTypeCode(5L);
			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");
			try{

				Optional<Transaction> transaction = transactionsService.findOne(transactionId);

				if(!transaction.isPresent()){
					apiErrorResponse.getFieldsErrors().put("transactionId", TRANSACTION_NOT_EXIST);
					return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
				}

				if(transaction.get().getType().equals(reverseType)){
					apiErrorResponse.getFieldsErrors().put("transactionId", TRANSACTION_IS_REVERSE);
					return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
				}

				Optional<Transaction> reverseTransaction = transactionsService.findByReversedTransactionId(transaction.get().getIdTransaction());
				if(!reverseTransaction.isPresent()){
					apiErrorResponse.getFieldsErrors().put("transactionId", TRANSACTION_NOT_REVERTED);
					return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
				}
				TransactionResponse response = new TransactionResponse(reverseTransaction.get().getIdTransaction(), reverseTransaction.get().getDateTransaction(),
					reverseTransaction.get().getType().getDescription());
				return new ResponseEntity<>(response, HttpStatus.OK);
			} catch(

			ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@RequestMapping(value = "/getTransaction/{transactionId}", method = RequestMethod.GET, produces = "application/json")
		@ApiOperation(value = "Consulta una transaccion")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error"),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> getTransaction(@PathVariable("transactionId") Long transactionId){

			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");

			Optional<Transaction> transaction = transactionsService.findOne(transactionId);

			if(!transaction.isPresent()){
				apiErrorResponse.getFieldsErrors().put("transactionId", TRANSACTION_NOT_EXIST);
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			}

			try{
				TransactionResponse response = new TransactionResponse(transaction.get().getIdTransaction(), transaction.get().getDateTransaction(), transaction.get().getType().getDescription());
				return new ResponseEntity<>(response, HttpStatus.OK);
			} catch(

			ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@RequestMapping(value = "/getTransactionByCharge/{chargeId}", method = RequestMethod.GET, produces = "application/json")
		@ApiOperation(value = "Consulta una transaccion")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error"),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> getTransactionByChargeId(@PathVariable("chargeId") Long chargeId){

			ApiError apiErrorResponse = new ApiError("111", "Ocurrio un error");

			Optional<Transaction> transaction = transactionsService.findOneByChargeId(chargeId);

			if(!transaction.isPresent()){
				apiErrorResponse.getFieldsErrors().put("transactionId", TRANSACTION_NOT_EXIST);
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			}

			try{
				TransactionResponse response = new TransactionResponse(transaction.get().getIdTransaction(), transaction.get().getDateTransaction(), transaction.get().getType().getDescription());
				return new ResponseEntity<>(response, HttpStatus.OK);
			} catch(

			ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@RequestMapping(value = "/generateMoney", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Crea dinero para la billetera de Nacion Servicios")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> generateMoney(@RequestBody GenerateMoneyRequest generateMoneyRequest, @CurrentUser UserDetails user){

			TransactionType codeTrasactionType = transactionsService.getTransactionTypeCode(4L);
			StringBuilder errorMessages = new StringBuilder();

			try{
				UserInfo userSubePagos = userDao.findOneByEmail(emailSubePagos);

				Wallet destinationWallet = walletService.getWalletByUserId(userSubePagos.getUserId());

				if(destinationWallet == null){
					errorMessages.append(DESTINATION_WALLET_NOT_EXISTS).append("\n");
					return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
				}

				operationService.generate(destinationWallet, generateMoneyRequest.getAmount(), codeTrasactionType.getTypeCode(), null, user.getUserId());

			} catch(ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError apiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);

				if(apiError.getFieldsErrors() != null && !apiError.getFieldsErrors().isEmpty()){
					apiError.getFieldsErrors().entrySet().stream().forEach(err -> errorMessages.append(err.getValue()).append("\n"));
				}

				errorMessages.append(apiError.getMessage());
				return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return new ResponseEntity<>("Dinero creado correctamente", HttpStatus.OK);

		}

		@RequestMapping(value = "/wireTransfer", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Realiza la transferencia de dinero a la billetera de Transferencias")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
				@ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> wireTransfer(@RequestBody WireTransferRequest wireTransferRequest) throws ParseException{

			LOGGER.info("decimal a transfereir : " + String.valueOf(wireTransferRequest.getAmountToTransfer()));

			TransactionType trasactionTypeCode = transactionsService.getTransactionTypeCode(7L);
			ApiErrorv2 errorResponse = new ApiErrorv2();
			StringBuilder errorMessages = new StringBuilder();

			try{

				UserInfo userInfo = userDao.findOneByEmail(emailWireTransfer);

				Wallet destinationWallet = walletService.getWalletByUserId(userInfo.getUserId());
				Wallet sourceWallet = walletService.getWalletByWallet(wireTransferRequest.getWalletId());

				if(sourceWallet == null){
					errorResponse.setTitle("Error en parametros de entrada");
					errorResponse.setType("Parameter");
					errorResponse.getDetail().put("walletId", SOURCE_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
				}

				if(destinationWallet == null){
					errorResponse.setTitle("Atributo requerido no encontrado");
					errorResponse.setType("Atributo");
					errorResponse.getDetail().put("destinationWallet", DESTINATION_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
				}

				if(sourceWallet.getIdWallet().compareTo(destinationWallet.getIdWallet()) == 0){
					errorResponse.setTitle("Error en parametros de entrada");
					errorResponse.setType("Parameter");
					errorResponse.getDetail().put("walletId", SAME_WALLET);
					return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
				}

				DataValidate dataValidate = new DataValidate(destinationWallet, sourceWallet, wireTransferRequest.getAmountToTransfer(), errorMessages, trasactionTypeCode);
				limitService.validateLimits(dataValidate);

				if(dataValidate.getErrorMessages().length() == 0){
					Transaction transaction = operationService.operate(sourceWallet, destinationWallet, wireTransferRequest.getAmountToTransfer(), trasactionTypeCode.getTypeCode(),
						wireTransferRequest.getTransferId(), null);

					return new ResponseEntity<>(transaction.getIdTransaction(), HttpStatus.OK);

				} else{
					errorResponse.setTitle("Error validando operacion");
					errorResponse.setType("Validacion");
					errorResponse.getDetail().put("errors", dataValidate.getErrorMessages().toString());
					return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
				}
			} catch(ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError apiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);

				if(apiError.getFieldsErrors() != null && !apiError.getFieldsErrors().isEmpty()){
					apiError.getFieldsErrors().entrySet().stream().forEach(err -> errorMessages.append(err.getValue()).append("\n"));
				}
				return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@RequestMapping(value = "/getTransactionToTransfer", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Devuelve las transacciones que ingresaran en una transferencia bancaria")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = TransactionToTransferResponse.class, responseContainer = "List"),
				@ApiResponse(code = 500, message = "Internal Server Error"), @ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
		})
		public @ResponseBody ResponseEntity<?> getTransactionToTransfer(@RequestBody GetTransactionToTransferRequest request) throws ParseException{

			ApiErrorv2 errorResponse = new ApiErrorv2();
			StringBuilder errorMessages = new StringBuilder();

			try{

				UserInfo userInfo = userDao.findOneByEmail(emailWireTransfer);

				Wallet destinationWallet = walletService.getWalletByUserId(userInfo.getUserId());
				Wallet sourceWallet = walletService.getWalletByWallet(request.getWalletId());

				if(sourceWallet == null){
					errorResponse.setTitle("Error en parametros de entrada");
					errorResponse.setType("Parameter");
					errorResponse.getDetail().put("walletId", SOURCE_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
				}

				if(destinationWallet == null){
					errorResponse.setTitle("Atributo requerido no encontrado");
					errorResponse.setType("Atributo");
					errorResponse.getDetail().put("destinationWallet", DESTINATION_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
				}

				if(sourceWallet.getIdWallet().compareTo(destinationWallet.getIdWallet()) == 0){
					errorResponse.setTitle("Error en parametros de entrada");
					errorResponse.setType("Parameter");
					errorResponse.getDetail().put("walletId", SAME_WALLET);
					return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
				}
				List<TransactionToTransferResponse> transactionToTransfer = transactionsService.getTransactionsToTransfer(sourceWallet, request.getDateTo(), request.getDateFrom()).stream()
					.map(dt -> new TransactionToTransferResponse(dt.getIdTransactionDetail(), dt.getAmount(), dt.isCredit())).collect(Collectors.toList());

				return new ResponseEntity<>(transactionToTransfer, HttpStatus.OK);

			} catch(ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError apiError = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);

				if(apiError.getFieldsErrors() != null && !apiError.getFieldsErrors().isEmpty()){
					apiError.getFieldsErrors().entrySet().stream().forEach(err -> errorMessages.append(err.getValue()).append("\n"));
				}
				return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}

		@RequestMapping(value = "/chargeWallet", method = RequestMethod.POST, produces = "application/json")
		@ApiOperation(value = "Carga de billeteras")
		@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = String.class), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
				@ApiResponse(code = 400, message = "Bad Request", response = String.class)
		})
		public @ResponseBody ResponseEntity<?> chargeWallet(@RequestBody InputChargeWalletRequest inputChargeWalletRequest){

			TransactionType codeTrasactionType = transactionsService.getTransactionTypeCode(6L);
			ApiErrorv2 apiError = new ApiErrorv2();
			StringBuilder errorMessages = new StringBuilder();
			try{

				if(inputChargeWalletRequest.getAmount().compareTo(new BigDecimal(0L)) < 1){
					apiError.setTitle("Error en parametros");
					apiError.setType("Parametro");
					apiError.getDetail().put("amount", INVALID_AMOUNT);
					return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
				}

				UserInfo userNssa = userDao.findOneByEmail(emailSubePagos);
				Wallet sourceWallet = walletService.getWalletByUserId(userNssa.getUserId());
				Wallet destinationWallet = walletService.getWalletByWallet(inputChargeWalletRequest.getIdWallet());

				if(destinationWallet == null){

					apiError.setTitle("Error en parametros");
					apiError.setType("Parametro");
					apiError.getDetail().put("walletId", DESTINATION_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
				}

				if(sourceWallet == null){

					apiError.setTitle("El atributo requerido no se encontro");
					apiError.setType("Atributo");
					apiError.getDetail().put("walletId", SOURCE_WALLET_NOT_EXISTS);
					return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);

				}

				DataValidate dataValidate = new DataValidate(destinationWallet, sourceWallet, inputChargeWalletRequest.getAmount(), errorMessages, codeTrasactionType);
				limitService.validateLimits(dataValidate);

				if(dataValidate.getErrorMessages().length() == 0){
					Transaction transaction = operationService.operate(sourceWallet, destinationWallet, inputChargeWalletRequest.getAmount(), codeTrasactionType.getTypeCode(), null, null);
					ChargeWalletResponse response = new ChargeWalletResponse(transaction.getIdTransaction());
					return new ResponseEntity<>(response.getIdTransaction(), HttpStatus.OK);
				} else{
					apiError.setTitle("Error validando operacion");
					apiError.setType("Validacion");
					apiError.getDetail().put("errors", dataValidate.getErrorMessages().toString());
					return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);

				}

			} catch(ObjectOptimisticLockingFailureException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				return new ResponseEntity<>(OPTIMISTIC_LOCK, HttpStatus.BAD_REQUEST);
			} catch(HttpClientErrorException e){
				LOGGER.info("Ocurrio un Error " + e.getMessage());
				ApiError apiErrorResponse = new Gson().fromJson(e.getResponseBodyAsString(), ApiError.class);

				if(apiErrorResponse.getFieldsErrors() != null && !apiErrorResponse.getFieldsErrors().isEmpty()){
					apiErrorResponse.getFieldsErrors().entrySet().stream().forEach(err -> errorMessages.append(err.getValue()).append("\n"));
				}

				errorMessages.append(apiErrorResponse.getMessage());
				return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
			} catch(Exception e){
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}

	}
