/*eslint-disable no-undef*/
const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const Dotenv = require('dotenv-webpack')

/*eslint-disable no-undef*/
module.exports = (env, argv = {}) => {
  const isProduction = argv.mode === 'production'

  // Production bundles carry a content hash so a new build lands on new URLs.
  // Without it every build emits the same `build/<id>.js` names, and a browser
  // holding the previous build in cache keeps serving it after an upgrade —
  // the fix looks like it never shipped. Hashes can't be combined with the dev
  // server's hot updates, so development keeps stable names.
  const bundleName = isProduction
    ? 'build/[name].[contenthash].js'
    : 'build/[name].js'

  return {
    entry: './src/index.js',
    resolve: {
      modules: [path.resolve(__dirname, 'src'), 'node_modules'],
      preferRelative: true,
    },
    output: {
      path: path.resolve(__dirname, 'dist'),
      filename: bundleName,
      chunkFilename: bundleName,
      publicPath: '/',
      // Hashed names accumulate across builds; drop stale ones so `dist` only
      // ever holds the current build.
      clean: true,
    },
    optimization: {
      splitChunks: {
        chunks: 'all',
        minSize: 10000,
        maxSize: 250000,
      },
    },
    module: {
      rules: [
        {
          test: /\.(js|jsx)$/,
          include: path.resolve(__dirname, 'src'),
          use: {
            loader: 'babel-loader',
            options: {
              presets: [
                '@babel/preset-env',
                ['@babel/preset-react', { runtime: 'automatic' }],
              ],
            },
          },
        },
        {
          test: /\.css$/,
          use: ['style-loader', 'css-loader'],
        },
        {
          test: /\.(png|svg|jpg|gif|svg)$/,
          use: ['file-loader'],
        },
        {
          test: /\.(woff(2)?|ttf|eot)(\?v=\d+\.\d+\.\d+)?$/,
          use: ['file-loader'],
        },
        {
          test: /favicon\.ico$/,
          use: [
            {
              loader: 'file-loader',
              options: {
                name: '[name].[ext]',
              },
            },
          ],
        },
      ],
    },
    plugins: [
      new HtmlWebpackPlugin({
        template: './public/index.html',
      }),
      new Dotenv({ systemvars: true }),
    ],
    devServer: {
      static: path.join(__dirname, 'public'),
      port: 3001,
      historyApiFallback: true,
    },
    devtool: 'source-map',
  }
}
