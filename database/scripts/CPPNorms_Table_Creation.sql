USE [RIL.AOP]
GO

/****** Object:  Table [dbo].[CPPNorms]    Script Date: 1/23/2026 12:42:00 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[CPPNorms](
    [Id] [uniqueidentifier] NOT NULL DEFAULT NEWID(),
    [NormsHeader_FK_Id] [uniqueidentifier] NOT NULL,
    [FinancialYear] [nvarchar](20) NOT NULL,
    [AOPYear] [nvarchar](20) NOT NULL,
    [NormType_FK_Id] [int] NOT NULL,
    
    -- Monthly Norms (Apr to Mar)
    [Apr_Norms] [decimal](18, 6) NULL,
    [May_Norms] [decimal](18, 6) NULL,
    [Jun_Norms] [decimal](18, 6) NULL,
    [Jul_Norms] [decimal](18, 6) NULL,
    [Aug_Norms] [decimal](18, 6) NULL,
    [Sep_Norms] [decimal](18, 6) NULL,
    [Oct_Norms] [decimal](18, 6) NULL,
    [Nov_Norms] [decimal](18, 6) NULL,
    [Dec_Norms] [decimal](18, 6) NULL,
    [Jan_Norms] [decimal](18, 6) NULL,
    [Feb_Norms] [decimal](18, 6) NULL,
    [Mar_Norms] [decimal](18, 6) NULL,
    
    [Remarks] [nvarchar](1000) NULL,
    
    [CreatedBy] [nvarchar](100) NULL,
    [CreatedDate] [datetime] NULL DEFAULT GETDATE(),
    [ModifiedBy] [nvarchar](100) NULL,
    [ModifiedDate] [datetime] NULL,
    
    CONSTRAINT [PK_CPPNorms] PRIMARY KEY CLUSTERED ([Id] ASC),
    CONSTRAINT [FK_CPPNorms_NormsHeader] FOREIGN KEY ([NormsHeader_FK_Id]) REFERENCES [dbo].[NormsHeader]([Id]),
    CONSTRAINT [FK_CPPNorms_NormTypes] FOREIGN KEY ([NormType_FK_Id]) REFERENCES [dbo].[NormTypes]([Id]),
    CONSTRAINT [UQ_CPPNorms_Header_Year] UNIQUE ([NormsHeader_FK_Id], [FinancialYear])
)
GO
